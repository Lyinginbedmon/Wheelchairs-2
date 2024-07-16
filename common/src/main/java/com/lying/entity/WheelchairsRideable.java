package com.lying.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public abstract class WheelchairsRideable extends LivingEntity
{
	protected WheelchairsRideable(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createMountAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 1F).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1F);
	}
	
	public abstract void copyFromItem(ItemStack stack);
	
	public abstract <T extends WheelchairsRideable> ItemStack entityToItem(T entity);
	
	/** Converts this wheelchair into an ItemEntity or (if a player is supplied) an ItemStack in a player's inventory */
	public void convertToItem(@Nullable PlayerEntity player)
	{
		if(getWorld().isClient())
			return;
		
		ItemStack stack = entityToItem(this);
		ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
		dropInventory();
		
		if(player == null || !player.getInventory().insertStack(stack))
			getWorld().spawnEntity(item);
		discard();
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		if(player.shouldCancelInteraction() && !hasPassengers())
		{
			convertToItem(null);
			return ActionResult.CONSUME;
		}
		else if(!this.getWorld().isClient() && !hasPassengers())
			return putPlayerInSaddle(player) ? ActionResult.CONSUME : ActionResult.PASS;
		
		return ActionResult.SUCCESS;
	}
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public boolean canStartRiding(Entity entity) { return false; }
	
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		super.tickControlled(controllingPlayer, movementMultiplier);
		orientToRider(controllingPlayer, movementInput);
	}
	
	protected void orientToRider(LivingEntity controllingPlayer, Vec3d movementInput)
	{
		Vec2f orientation = getControlledRotation(controllingPlayer);
		this.setRotation(orientation.y, orientation.x);
		this.prevYaw = this.headYaw;
		this.bodyYaw = this.headYaw = this.getYaw();
	}
	
	public boolean isSaddled() { return true; }
	
	public int getDefaultPortalCooldown() { return 10; }
	
	/** Identical to standard behaviour, except can use portals whilst ridden */
	public boolean canUsePortals() { return !hasVehicle() && !isSleeping(); }
	
	public Entity moveToWorld(ServerWorld destination)
	{
		if(!(getWorld() instanceof ServerWorld) || isRemoved())
			return null;
		else if(!hasPassengers())
			return super.moveToWorld(destination);
		
		Profiler profiler = getWorld().getProfiler();
		profiler.push("changeDimension");
		if(hasVehicle())
			dismountVehicle();
		profiler.push("reposition");
		TeleportTarget teleportTarget = getTeleportTarget(destination);
		if(teleportTarget == null)
			return null;
		profiler.swap("reloading");
		Entity entity = recreateInDimension(destination);
		if(entity != null)
		{
			ServerPlayerEntity player = null;
			if(getPlayerPassengers() > 0)
			{
				player = (ServerPlayerEntity)getFirstPassenger();
				player.dismountVehicle();
			}
			
			entity.refreshPositionAndAngles(teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z, teleportTarget.yaw, entity.getPitch());
			entity.setVelocity(teleportTarget.velocity);
			destination.spawnNewEntityAndPassengers(entity);
			if(destination.getRegistryKey() == World.END)
				ServerWorld.createEndSpawnPlatform(destination);
			
			if(player != null)
			{
				Vector3f seatOffset = getPassengerAttachmentPos(player, entity.getDimensions(EntityPose.STANDING), 1F);
				Vec3d offsetPos = entity.getPos().add(seatOffset.x, seatOffset.y, seatOffset.z);
				player.teleport(destination, offsetPos.x, offsetPos.y, offsetPos.z, entity.getYaw(), entity.getPitch());
				player.startRiding(entity);
			}
		}
		removeFromDimension();
		profiler.pop();
		((ServerWorld)getWorld()).resetIdleTimeout();
		destination.resetIdleTimeout();
		profiler.pop();
		return entity;
	}
	
	public int getPlayerPassengers() { return (int)getPassengerList().stream().filter(Entity::isPlayer).count(); }
	
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor)
	{
		return new Vector3f(0f, dimensions.height, 0f);
	}
	
	protected Entity recreateInDimension(ServerWorld destination)
	{
		NbtCompound chairData = new NbtCompound();
		saveNbt(chairData);
		
		return EntityType.loadEntityWithPassengers(chairData, destination, entity -> {
			entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
			return entity;
		});
	}
	
	protected boolean putPlayerInSaddle(PlayerEntity player)
	{
		if(hasPassengers() || getWorld().isClient())
			return false;
		
		player.setYaw(this.getYaw());
		player.setPitch(this.getPitch());
		player.startRiding(this);
		return true;
	}
	
	protected boolean canAddPassenger(Entity passenger) { return !hasPassengers() && passenger instanceof LivingEntity; }
	
	protected Vec2f getControlledRotation(LivingEntity controllingPassenger)
	{
		return new Vec2f(controllingPassenger.getPitch(), controllingPassenger.getYaw());
	}
	
	public Vec3d updatePassengerForDismount(LivingEntity passenger)
	{
		Vec3d dismountRight = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.0f : -90.0f));
		Vec3d dismountPos = this.locateSafeDismountingPos(dismountRight, passenger);
		if(dismountPos != null)
			return dismountPos;
		
		Vec3d dismountLeft = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.0f : -90.0f));
		dismountPos = this.locateSafeDismountingPos(dismountLeft, passenger);
		if(dismountPos != null)
			return dismountPos;
		
		return this.getPos();
	}
	
	@Nullable
	private Vec3d locateSafeDismountingPos(Vec3d offset, LivingEntity passenger)
	{
		double d = this.getX() + offset.x;
		double e = this.getBoundingBox().minY;
		double f = this.getZ() + offset.z;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		block0: for(EntityPose entityPose : passenger.getPoses())
		{
			mutable.set(d, e, f);
			double g = this.getBoundingBox().maxY + 0.75;
			do
			{
				double h = this.getWorld().getDismountHeight(mutable);
				if((double)mutable.getY() + h > g)
					continue block0;
				
				if(Dismounting.canDismountInBlock(h))
				{
					Box box = passenger.getBoundingBox(entityPose);
					Vec3d vec3d = new Vec3d(d, (double)mutable.getY() + h, f);
					if(Dismounting.canPlaceEntityAt(this.getWorld(), passenger, box.offset(vec3d)))
					{
						passenger.setPose(entityPose);
						return vec3d;
					}
				}
				mutable.move(Direction.UP);
			}
			while ((double)mutable.getY() < g);
		}
		return null;
	}
	
	protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater)
	{
		super.updatePassengerPosition(passenger, positionUpdater);
		if(passenger instanceof LivingEntity)
			clampPassengerYaw(passenger);
	}
	
	public void onPassengerLookAround(Entity passenger)
	{
		clampPassengerYaw(passenger);
	}
	
	protected abstract void clampPassengerYaw(Entity passenger);
	
	public boolean isInvulnerableTo(DamageSource damageSource)
	{
		DamageSources sources = getWorld().getDamageSources();
		return !(
				damageSource == sources.outOfWorld() ||
				damageSource == sources.genericKill()
				);
	}
	
	public boolean canHaveStatusEffect(StatusEffectInstance effect) { return false; }
	
	public int getAir() { return 20; }
	
	protected void tickExhaustion(double deltaX, double deltaZ)
	{
		if(getWorld().isClient() || !hasPassengers()) return;
		
		PlayerEntity rider = getControllingPassenger() == null || getControllingPassenger().getType() != EntityType.PLAYER ? null : (PlayerEntity)getControllingPassenger();
		if(rider == null)
			return;
		
		int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 100.0f);
		if(i > 0)
		{
			float exhaust = 0F;
			if(rider.isSprinting())
				exhaust = 0.1F * (float)i * 0.01F;
			else if(rider.isSneaking())	// These last two formulae always equal zero but are here for consistency with {@link ServerPlayerEntity}
				exhaust = 0.0f * (float)i * 0.01f;
			else
				exhaust = 0F * (float)i * 0.01F;
			
			rider.addExhaustion(exhaust);
		}
	}
	
	public void move(MovementType type, Vec3d movementInput)
	{
		double x = getX();
		double z = getZ();
		
		// Adjust movement input to prevent dangerous collision for any passenger
		for(Entity rider : getPassengerList())
			movementInput = rider.adjustMovementForCollisions(movementInput);
		
		super.move(type, movementInput);
		this.tickExhaustion(getX() - x, getZ() - z);
	}
	
	public float getStepHeight()
	{
		float stepHeight = getActualStepHeight();
		// Disable step-up if it would cause any passenger to bang their head
		return getPassengerList().stream().anyMatch(rider -> WheelchairsRideable.willSuffocateRider(rider.getBoundingBox(), new Vec3d(0, stepHeight, 0), getWorld())) ? 0F : stepHeight;
	}
	
	/**
	 * Returns true if a movement in the given direction would result in suffocation for an entity with the given bounding box
	 * @param riderBounds
	 * @param offset
	 * @param world
	 * @return
	 */
	public static boolean willSuffocateRider(Box riderBounds, Vec3d offset, World world)
	{
		Box bounds = riderBounds.offset(offset);
		return BlockPos.stream(bounds).anyMatch(pos -> 
		{
			BlockState state = world.getBlockState(pos);
			return
					!state.isAir() &&
					state.shouldSuffocate(world, pos) &&
					VoxelShapes.matchesAnywhere(state.getCollisionShape(world, (BlockPos)pos).offset(pos.getX(), pos.getY(), pos.getZ()), VoxelShapes.cuboid(bounds), BooleanBiFunction.AND);
		});
	}
	
	public abstract float getActualStepHeight();
}
