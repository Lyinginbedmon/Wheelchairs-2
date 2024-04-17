package com.lying.wheelchairs.entity;

import java.util.Map;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Mount;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class EntityWheelchair extends LivingEntity implements Mount, ItemSteerable
{
	private static final TrackedData<ItemStack> CHAIR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	private static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	private static final TrackedData<Boolean> POWERED = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.INTEGER);
	private final SaddledComponent saddledComponent;
	
	public EntityWheelchair(EntityType<? extends EntityWheelchair> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(1.0f);
		this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, POWERED);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		
		this.getDataTracker().startTracking(CHAIR, WHCItems.WHEELCHAIR_OAK.getDefaultStack());
		this.getDataTracker().startTracking(COLOR, OptionalInt.of(DyeableItem.DEFAULT_COLOR));
		this.getDataTracker().startTracking(LEFT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
		this.getDataTracker().startTracking(RIGHT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
		
		this.getDataTracker().startTracking(POWERED, false);
		this.getDataTracker().startTracking(BOOST_TIME, 0);
	}
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(BOOST_TIME.equals(data) && getWorld().isClient())
			this.saddledComponent.boost();
		super.onTrackedDataSet(data);
	}
	
	public static DefaultAttributeContainer.Builder createChairAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 1F).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1F);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("Chair", NbtElement.COMPOUND_TYPE))
			getDataTracker().set(CHAIR, ItemStack.fromNbt(data.getCompound("Chair")));
		
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		
		if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound wheels = data.getCompound("Wheels");
			getDataTracker().set(LEFT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Left")));
			getDataTracker().set(RIGHT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Right")));
		}
		
		getDataTracker().set(POWERED, data.getBoolean("Powered"));
		this.saddledComponent.readNbt(data);
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.put("Chair", getDataTracker().get(CHAIR).writeNbt(new NbtCompound()));
		if(getDataTracker().get(COLOR).isPresent())
			data.putInt("Color", getColor());
		NbtCompound wheels = new NbtCompound();
			wheels.put("Left", getLeftWheel().writeNbt(new NbtCompound()));
			wheels.put("Right", getRightWheel().writeNbt(new NbtCompound()));
		data.put("Wheels", wheels);
		data.putBoolean("Powered", getDataTracker().get(POWERED));
		this.saddledComponent.writeNbt(data);
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getStackInHand(hand);
		if(heldStack.getItem() == Items.FURNACE_MINECART && player.shouldCancelInteraction() && !saddledComponent.isSaddled())
		{
			getDataTracker().set(POWERED, true);
			if(!player.getAbilities().creativeMode)
				heldStack.decrement(1);
			return ActionResult.success(getWorld().isClient());
		}
		
		if(!hasPassengers())
			if(player.shouldCancelInteraction())
			{
				ItemStack stack = chairToItem(this);
				
				ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
				if(!getWorld().isClient())
				{
					getWorld().spawnEntity(item);
					discard();
				}
				return ActionResult.CONSUME;
			}
			else if(!this.getWorld().isClient())
				return putPlayerInChair(player) ? ActionResult.CONSUME : ActionResult.PASS;
		
		return ActionResult.SUCCESS;
	}
	
	public static ItemStack chairToItem(EntityWheelchair chair)
	{
		ItemStack stack = chair.getChair();
		ItemWheelchair.setWheels(stack, chair.getLeftWheel(), chair.getRightWheel());
		if(chair.hasColor() && stack.getItem() instanceof ItemWheelchair)
			((ItemWheelchair)stack.getItem()).setColor(stack, chair.getColor());
		
		NbtList upgrades = new NbtList();
		if(chair.saddledComponent.isSaddled())
			upgrades.add(NbtString.of(Upgrades.POWERED.asString()));
		stack.getNbt().put("Upgrades", upgrades);
		
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(CHAIR, stack);
		getDataTracker().set(COLOR, stack.getItem() instanceof DyeableItem ? OptionalInt.of(((DyeableItem)stack.getItem()).getColor(stack)) : OptionalInt.empty());
		getDataTracker().set(LEFT_WHEEL, ItemWheelchair.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, ItemWheelchair.getWheel(stack, Arm.RIGHT));
		
		NbtCompound stackData = stack.getNbt();
		if(stackData.contains("Upgrades", NbtElement.LIST_TYPE))
		{
			NbtList upgrades = stackData.getList("Upgrades", NbtElement.STRING_TYPE);
			for(int i=0; i<upgrades.size(); i++)
			{
				Upgrades upgrade = Upgrades.fromString(upgrades.getString(i));
				switch(upgrade)
				{
					case POWERED:
						getDataTracker().set(POWERED, true);
					default:
						break;
				}
			}
		}
	}
	
	protected boolean putPlayerInChair(PlayerEntity player)
	{
		if(hasPassengers() || getWorld().isClient())
			return false;
		
		player.setYaw(this.getYaw());
		player.setPitch(this.getPitch());
		player.startRiding(this);
		return true;
	}
	
	protected boolean canAddPassenger(Entity passenger) { return !hasPassengers() && passenger instanceof LivingEntity; }
	
	public LivingEntity getControllingPassenger()
	{
		return getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : null;
	}
	
	public int getDefaultPortalCooldown() { return 10; }
	
	/** Returns true if the wheelchair is under manual control ie. not using a wheelchair controller */
	public boolean isManual(PlayerEntity controllingPlayer) { return !(controllingPlayer.isHolding(WHCItems.CONTROLLER) && saddledComponent.isSaddled()); }
	
	// FIXME Ensure that manual control appropriately uses up player hunger
	public void tickMovement()
	{
		double deltaX = this.getX();
		double deltaY = this.getY();
		double deltaZ = this.getZ();
		super.tickMovement();
		deltaX = getX() - deltaX;
		deltaY = getY() - deltaY;
		deltaZ = getZ() - deltaZ;
		
		if(hasPassengers())
		{
			PlayerEntity controllingPlayer = getControllingPassenger() == null || getControllingPassenger().getType() != EntityType.PLAYER ? null : (PlayerEntity)getControllingPassenger();
			if(controllingPlayer == null || !isManual(controllingPlayer))
				return;
			
			int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 100.0f);
			if(i > 0)
				if(controllingPlayer.isSprinting())
					controllingPlayer.getHungerManager().addExhaustion(0.1F * (float)i * 0.01F);
				else
					controllingPlayer.getHungerManager().addExhaustion(0F * (float)i * 0.01F);
		}
	}
	
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		super.tickControlled(controllingPlayer, movementMultiplier);
		if(!isManual(controllingPlayer) && isSprinting())
		{
			setSprinting(false);
			controllingPlayer.setSprinting(false);
		}
		
		Vec2f orientation = getControlledRotation(controllingPlayer);
		this.setRotation(orientation.y, orientation.x);
		this.bodyYaw = this.headYaw = this.getYaw();
		this.prevYaw = this.headYaw;
		
		this.saddledComponent.tickBoost();
	}
	
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
	
	private Entity recreateInDimension(ServerWorld destination)
	{
		NbtCompound chairData = new NbtCompound();
		saveNbt(chairData);
		
		return EntityType.loadEntityWithPassengers(chairData, destination, entity -> {
			entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
            return entity;
        });
	}
	
	public boolean isSaddled() { return true; }
	
	protected Vec2f getControlledRotation(LivingEntity controllingPassenger)
	{
		return new Vec2f(controllingPassenger.getPitch(), controllingPassenger.getYaw());
	}
	
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		if(isOnGround())
		{
			if(!isManual(controllingPlayer))
				return new Vec3d(0, 0, 1.0D);
			else
				return new Vec3d(0, 0, controllingPlayer.forwardSpeed);
		}
		return Vec3d.ZERO;
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (isManual(controllingPlayer) ? 1 : this.saddledComponent.getMovementSpeedMultiplier());
	}
	
	public boolean canSprintAsVehicle()
	{
		LivingEntity player = getControllingPassenger();
		return (player.getType() == EntityType.PLAYER && isManual((PlayerEntity)player)) && !saddledComponent.isSaddled();
	}
	
	public int getEnchantmentLevel(Enchantment ench)
	{
		return Math.max(EnchantmentHelper.getLevel(ench, getWheel(Arm.LEFT)), EnchantmentHelper.getLevel(ench, getWheel(Arm.RIGHT)));
	}
	
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor)
	{
		return new Vector3f(0F, dimensions.height * 0.85F * scaleFactor, 0F);
	}
	
	protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater)
	{
		super.updatePassengerPosition(passenger, positionUpdater);
		if(passenger instanceof LivingEntity)
			((LivingEntity)passenger).bodyYaw = this.bodyYaw;
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
	
	public boolean isInvulnerableTo(DamageSource damageSource)
	{
		DamageSources sources = getWorld().getDamageSources();
		return !(
				damageSource == sources.outOfWorld() ||
				damageSource == sources.genericKill()
				);
	}
	
	public boolean canHaveStatusEffect(StatusEffectInstance effect) { return false; }
	
	public boolean hasStatusEffect(StatusEffect effect)
	{
		if(hasPassengers() && getControllingPassenger() instanceof LivingEntity)
			return getControllingPassenger().hasStatusEffect(effect);
		return false;
	}
	
	public StatusEffectInstance getStatusEffect(StatusEffect effect)
	{
		if(hasPassengers() && getControllingPassenger() instanceof LivingEntity)
			return getControllingPassenger().getStatusEffect(effect);
		return null;
	}
	
	public int getAir() { return 20; }
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(4, ItemStack.EMPTY); }
	
	public boolean canEquip(ItemStack stack) { return false; }
	
	public ItemStack getEquippedStack(EquipmentSlot slot)
	{
		return slot == EquipmentSlot.FEET ? getEnchantments(getWheel(Arm.LEFT), getWheel(Arm.RIGHT)) : ItemStack.EMPTY;
	}
	
	public static ItemStack getEnchantments(ItemStack leftWheel, ItemStack rightWheel)
	{
		ItemStack spoof = Items.STONE.getDefaultStack();
		
		Map<Enchantment, Integer> ench = EnchantmentHelper.get(leftWheel);
		EnchantmentHelper.get(rightWheel).forEach((enchant,lvl) -> {
			if(lvl > ench.get(enchant))
				ench.put(enchant, lvl);
		});
		ench.forEach((enchant, lvl) -> spoof.addEnchantment(enchant, lvl));
		
		return spoof;
	}
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public ItemStack getChair()
	{
		ItemStack stack = getDataTracker().get(CHAIR);
		return stack.getItem() instanceof ItemWheelchair ? stack : new ItemStack(WHCItems.WHEELCHAIR_OAK);
	}
	
	public boolean hasColor() { return getDataTracker().get(COLOR).isPresent(); }
	public int getColor() { return getDataTracker().get(COLOR).orElse(-1); }
	
	public ItemStack getWheel(Arm arm) { return arm == Arm.LEFT ? getLeftWheel() : getRightWheel(); }
	public ItemStack getLeftWheel() { return getDataTracker().get(LEFT_WHEEL); }
	public ItemStack getRightWheel() { return getDataTracker().get(RIGHT_WHEEL); }
	
	public boolean consumeOnAStickItem() { return this.saddledComponent.boost(this.getRandom()); }
	
	public static enum Upgrades implements StringIdentifiable
	{
		POWERED;

		@Override
		public String asString() { return name().toLowerCase(); }
		
		public Text translate() { return Text.translatable("upgrade."+Reference.ModInfo.MOD_ID+"."+asString()); }
		
		@Nullable
		public static Upgrades fromString(String nameIn)
		{
			for(Upgrades upgrade : values())
				if(upgrade.asString().equalsIgnoreCase(nameIn))
					return upgrade;
			return null;
		}
	}
}
