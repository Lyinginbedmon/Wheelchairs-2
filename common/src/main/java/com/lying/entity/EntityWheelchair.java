package com.lying.entity;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.lying.block.BlockFrostedLava;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCItems;
import com.lying.init.WHCUpgrades;
import com.lying.item.ItemWheelchair;
import com.lying.reference.Reference;
import com.lying.utility.ServerBus;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
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
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class EntityWheelchair extends LivingEntity implements JumpingMount, ItemSteerable, IFlyingMount
{
	private static final TrackedData<ItemStack> CHAIR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	private static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	private static final TrackedData<NbtCompound> UPGRADES = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	public static final TrackedData<Boolean> POWERED = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Boolean> FLYING = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.INTEGER);
	private final SaddledComponent saddledComponent;
	
	protected SimpleInventory items;
	protected float jumpStrength = 0F;
	
	public float spinLeft, spinRight;
	
	public EntityWheelchair(EntityType<? extends EntityWheelchair> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(1.0f);
		this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, POWERED);
		this.onChestedStatusChanged();
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		
		this.getDataTracker().startTracking(CHAIR, WHCItems.WHEELCHAIR_OAK.get().getDefaultStack());
		this.getDataTracker().startTracking(COLOR, OptionalInt.of(DyeableItem.DEFAULT_COLOR));
		this.getDataTracker().startTracking(LEFT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK.get()));
		this.getDataTracker().startTracking(RIGHT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK.get()));
		
		this.getDataTracker().startTracking(UPGRADES, new NbtCompound());
		this.getDataTracker().startTracking(POWERED, false);
		this.getDataTracker().startTracking(FLYING, false);
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
		
		if(data.contains("Upgrades", NbtElement.LIST_TYPE))
			setUpgrades(data.getList("Upgrades", NbtElement.STRING_TYPE));
		
		this.onChestedStatusChanged();
		if(this.hasUpgrade(WHCUpgrades.STORAGE.get()))
		{
			NbtList items = data.getList("Items", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<items.size(); ++i)
			{
				NbtCompound nbt = items.getCompound(i);
				int j = nbt.getByte("Slot") & 0xFF;
				if (j < this.items.size())
					this.items.setStack(j, ItemStack.fromNbt(nbt));
			}
		}
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
		
		data.put("Upgrades", getUpgradeList());
		if(hasInventory())
		{
			NbtList items = new NbtList();
			for(int i=0; i<this.items.size(); ++i)
			{
				ItemStack stack = this.items.getStack(i);
				if(stack.isEmpty()) continue;
				NbtCompound nbt = new NbtCompound();
				nbt.putByte("Slot", (byte)i);
				stack.writeNbt(nbt);
				items.add(nbt);
			}
			data.put("Items", items);
		}
	}
	
	protected void setUpgrades(NbtList data)
	{
		List<ChairUpgrade> oldSet = getUpgrades();
		List<ChairUpgrade> newSet = WHCUpgrades.nbtToList(data);
		
		// Remove any upgrades currently applied that aren't in the new set
		oldSet.forEach(upgrade -> 
		{
			if(!newSet.contains(upgrade))
				upgrade.removeFrom(this);
		});
		
		NbtCompound upgrades = new NbtCompound();
		NbtList set = new NbtList();
		for(int i=0; i<data.size(); i++)
		{
			String name = data.getString(i);
			ChairUpgrade upgrade = WHCUpgrades.get(new Identifier(name));
			if(upgrade == null)
				continue;
			
			if(!oldSet.contains(upgrade))
				upgrade.applyTo(this);
			set.add(NbtString.of(name));
		}
		
		upgrades.put("Set", set);
		getDataTracker().set(UPGRADES, upgrades);
	}
	
	public NbtList getUpgradeList()
	{
		NbtCompound data = getDataTracker().get(UPGRADES);
		if(data.contains("Set", NbtElement.LIST_TYPE))
			return data.getList("Set", NbtElement.STRING_TYPE);
		else
			return new NbtList();
	}
	
	public List<ChairUpgrade> getUpgrades() { return WHCUpgrades.nbtToList(getUpgradeList()); }
	
	public final boolean hasUpgrade(RegistrySupplier<ChairUpgrade> upgrade) { return hasUpgrade(upgrade.get()); }
	
	public boolean hasUpgrade(ChairUpgrade upgrade)
	{
		return hasUpgrade(upgrade.registryName());
	}
	
	public boolean hasUpgrade(Identifier upgrade)
	{
		NbtList upgradeList = getUpgradeList();
		for(int i=0; i<upgradeList.size(); i++)
			if(upgradeList.getString(i).equals(upgrade.toString()))
				return true;
		return false;
	}
	
	public void addUpgrade(ChairUpgrade upgrade)
	{
		if(hasUpgrade(upgrade))
			return;
		
		NbtList upgrades = getUpgradeList();
		upgrades.add(NbtString.of(upgrade.registryName().toString()));
		setUpgrades(upgrades);
		onChestedStatusChanged();
		
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON, getSoundVolume(), getSoundPitch());
	}
	
	/** Returns true if this wheelchair has the Storage upgrade */
	public boolean hasInventory() { return hasUpgrade(WHCUpgrades.STORAGE.get()); }
	
	public Inventory getInventory() { return this.items; }
	
	protected void onChestedStatusChanged()
	{
		SimpleInventory inv = this.items;
		this.items = new SimpleInventory(15);
		if(inv != null)
			for(int j=0; j<Math.min(inv.size(), this.items.size()); ++j)
			{
				ItemStack stack = inv.getStack(j);
				if(!stack.isEmpty())
					this.items.setStack(j, stack.copy());
			}
	}
	
	public void dropInventory()
	{
		super.dropInventory();
		if(this.items != null)
			for(int i=0; i<this.items.size(); ++i)
			{
				ItemStack stack = this.items.getStack(i);
				if(stack.isEmpty() || EnchantmentHelper.hasVanishingCurse(stack)) continue;
				this.dropStack(stack);
				this.items.setStack(i, ItemStack.EMPTY);
			}
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getStackInHand(hand);
		if(player.shouldCancelInteraction())
		{
			List<ChairUpgrade> possibleUpgrades = Lists.newArrayList();
			possibleUpgrades.addAll(WHCUpgrades.fromItem(heldStack, this));
			if(!possibleUpgrades.isEmpty())
			{
				addUpgrade(possibleUpgrades.stream().findFirst().get());
				if(!player.getAbilities().creativeMode)
					heldStack.decrement(1);
				return ActionResult.success(getWorld().isClient());
			}
		}
		
		if(!hasPassengers())
			if(player.shouldCancelInteraction())
			{
				this.convertToItem(null);
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
		
		stack.getNbt().put("Upgrades", chair.getUpgradeList());
		
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(CHAIR, stack.copy());
		getDataTracker().set(COLOR, stack.getItem() instanceof DyeableItem ? OptionalInt.of(((DyeableItem)stack.getItem()).getColor(stack)) : OptionalInt.empty());
		getDataTracker().set(LEFT_WHEEL, ItemWheelchair.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, ItemWheelchair.getWheel(stack, Arm.RIGHT));
		
		NbtCompound stackData;
		if(stack.hasNbt() && (stackData = stack.getNbt()).contains("Upgrades", NbtElement.LIST_TYPE))
			setUpgrades(stackData.getList("Upgrades", NbtElement.STRING_TYPE));
	}
	
	/** Converts this wheelchair into an ItemEntity or (if a player is supplied) an ItemStack in a player's inventory */
	public void convertToItem(@Nullable PlayerEntity player)
	{
		if(!getWorld().isClient())
		{
			ItemStack stack = chairToItem(this);
			ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
			dropInventory();
			
			if(player == null || !player.getInventory().insertStack(stack))
				getWorld().spawnEntity(item);
			discard();
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
		return !isFallFlying() && getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : null;
	}
	
	public void tickMovement()
	{
		LivingEntity rider = null;
		if(isFallFlying() && (rider = (LivingEntity)getFirstPassenger()) != null)
			orientToRider(rider, Vec3d.ZERO);
		super.tickMovement();
	}
	
	public int getDefaultPortalCooldown() { return 10; }
	
	public boolean canStartRiding(Entity entity) { return false; }
	
	/** Returns true if the wheelchair is under manual control ie. not using a chair controller */
	public boolean isManual(PlayerEntity controllingPlayer) { return !isAutomatic(controllingPlayer); }
	
	/** Returns true if the wheelchair is under automatic control ie. using a chair controller*/
	public boolean isAutomatic(PlayerEntity controllingPlayer) { return hasUpgrade(WHCUpgrades.POWERED.get()) && controllingPlayer.isHolding(WHCItems.CONTROLLER.get()); }
	
	public boolean isSneaking() { return super.isSneaking() || hasPassengers() && getFirstPassenger() instanceof LivingEntity && getFirstPassenger().isSneaking(); }
	
	public void tick()
	{
		super.tick();
		
		if(this.saddledComponent.getMovementSpeedMultiplier() > 1F)
			getWorld().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0.0, 0.0, 0.0);
		
		if(hasUpgrade(WHCUpgrades.DIVING.get()) && isSubmergedIn(FluidTags.WATER))
			getWorld().addParticle(ParticleTypes.BUBBLE, getX(), getY() + 1.5D, getZ(), 0.0, 0.0, 0.0);
	}
	
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		super.tickControlled(controllingPlayer, movementMultiplier);
		if(!isManual(controllingPlayer) && isSprinting())
		{
			setSprinting(false);
			controllingPlayer.setSprinting(false);
		}
		
		orientToRider(controllingPlayer, movementInput);
		
		this.saddledComponent.tickBoost();
		
		ItemStack chair = getChair();
		if(!controllingPlayer.isOnFire() && EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, chair) > 0)
			controllingPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 5 * Reference.Values.TICKS_PER_SECOND * EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, chair), 0, false, false, true));
		if(!isSubmergedIn(FluidTags.WATER) && EnchantmentHelper.getLevel(Enchantments.RESPIRATION, chair) > 0)
			controllingPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 5 * Reference.Values.TICKS_PER_SECOND * EnchantmentHelper.getLevel(Enchantments.RESPIRATION, chair), 0, false, false, true));
		
		if(this.jumpStrength > 0F)
		{
				if(!isOnGround() && canStartFlying())
				{
					ServerBus.ON_DOUBLE_JUMP.invoker().onDoubleJump(this);
					startFlying();
				}
				else
					jump();
		}
		this.jumpStrength = 0F;
	}
	
	protected void orientToRider(LivingEntity controllingPlayer, Vec3d movementInput)
	{
		Vec2f orientation = getControlledRotation(controllingPlayer);
		if(movementInput.length() > 0 || !hasUpgrade(WHCUpgrades.POWERED.get()))
		{
			this.setRotation(orientation.y, orientation.x);
			this.prevYaw = this.headYaw;
			this.bodyYaw = this.headYaw = this.getYaw();
		}
	}
	
	public boolean isInSneakingPose() { return super.isInSneakingPose() || this.isSneaking(); }
	
	protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type)
	{
		if(movement.y <= 0 && (type == MovementType.SELF || type == MovementType.PLAYER) && isSneaking() && this.shouldClipMovement())
		{
			double deltaX = movement.x;
			double deltaZ = movement.z;
            while (deltaX != 0.0 && this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(deltaX, -this.getStepHeight(), 0.0))) {
                if (deltaX < 0.05 && deltaX >= -0.05) {
                	deltaX = 0.0;
                    continue;
                }
                if (deltaX > 0.0) {
                	deltaX -= 0.05;
                    continue;
                }
                deltaX += 0.05;
            }
            while (deltaZ != 0.0 && this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(0.0, -this.getStepHeight(), deltaZ))) {
                if (deltaZ < 0.05 && deltaZ >= -0.05) {
                    deltaZ = 0.0;
                    continue;
                }
                if (deltaZ > 0.0) {
                    deltaZ -= 0.05;
                    continue;
                }
                deltaZ += 0.05;
            }
            while (deltaX != 0.0 && deltaZ != 0.0 && this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(deltaX, -this.getStepHeight(), deltaZ))) {
                deltaX = deltaX < 0.05 && deltaX >= -0.05 ? 0.0 : (deltaX > 0.0 ? (deltaX -= 0.05) : (deltaX += 0.05));
                if (deltaZ < 0.05 && deltaZ >= -0.05) {
                    deltaZ = 0.0;
                    continue;
                }
                if (deltaZ > 0.0) {
                    deltaZ -= 0.05;
                    continue;
                }
                deltaZ += 0.05;
            }
            movement = new Vec3d(deltaX, movement.y, deltaZ);
		}
		return movement;
	}
	
	public boolean shouldClipMovement()
	{
		return isOnGround() || this.fallDistance < this.getStepHeight() && !this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(0, this.fallDistance - this.getStepHeight(), 0));
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
	
	protected void clampPassengerYaw(Entity passenger)
	{
		passenger.setBodyYaw(this.getYaw());
		float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
		float g = MathHelper.clamp(f, -105F, 105F);
		passenger.prevYaw += g - f;
		passenger.setYaw(passenger.getYaw() + g - f);
		passenger.setHeadYaw(passenger.getYaw());
	}
	
	public void setRotation(float yaw, float pitch)
	{
		float prevYaw = getYaw();
		super.setRotation(yaw, pitch);
		
		float amount = (getYaw() - prevYaw);
		if(amount == 0F || amount == 360F)
			return;
		
		if(isFallFlying()) return;
		this.spinLeft = clampRotation(this.spinLeft + amount);
		this.spinRight = clampRotation(this.spinRight - amount);
	}
	
	/** Identical to standard behaviour, except can use portals whilst ridden */
	public boolean canUsePortals() { return !hasVehicle() && !isSleeping(); }
	
	public boolean isClimbing() { return super.isClimbing() && !hasUpgrade(WHCUpgrades.POWERED.get()); }
	
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
	
	public int getPlayerPassengers()
	{
		return (int)streamIntoPassengers().filter(Entity::isPlayer).count();
	}
	
	private Stream<Entity> streamIntoPassengers()
	{
		return this.getPassengerList().stream().flatMap(Entity::streamSelfAndPassengers);
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
		double modifier = 1D;
		if(!isOnGround() && !hasUpgrade(WHCUpgrades.GLIDING.get()))
			if(hasUpgrade(WHCUpgrades.FLOATING.get()) && getFluidHeight(FluidTags.WATER) > 0D)
				modifier = 0.9D;
			else
				modifier = 0.7D;
		
		Vec3d speed = isAutomatic(controllingPlayer) ? new Vec3d(0, 0, 1D) : new Vec3d(0, 0, controllingPlayer.forwardSpeed);
		return speed.multiply(modifier);
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (isManual(controllingPlayer) ? 1F : this.saddledComponent.getMovementSpeedMultiplier());
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
		float stepHeight = super.getStepHeight();
		// Disable step-up if it would cause any passenger to bang their head
		return getPassengerList().stream().anyMatch(rider -> willSuffocateRider(rider.getBoundingBox(), new Vec3d(0, stepHeight, 0), getWorld())) ? 0F : stepHeight;
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
	
	public void travel(Vec3d movementInput)
	{
		if(hasUpgrade(WHCUpgrades.FLOATING.get()) && getFluidHeight(FluidTags.WATER) > getSwimHeight())
			addVelocity(0D, 0.08D, 0D);
		super.travel(movementInput);
		
		if(isFallFlying()) return;
		double speed = movementInput.getZ() * getMovementSpeed();
		this.spinLeft = addSpin(this.spinLeft, (float)speed);
		this.spinRight = addSpin(this.spinRight, (float)speed);
	}
	
	public void applyMovementEffects(BlockPos pos)
	{
		super.applyMovementEffects(pos);
		if(EnchantmentHelper.getLevel(Enchantments.FROST_WALKER, getChair()) > 0 && hasUpgrade(WHCUpgrades.NETHERITE.get()))
			freezeLava(this, getWorld(), getBlockPos(), EnchantmentHelper.getLevel(Enchantments.FROST_WALKER, getChair()));
	}
	
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource)
	{
		if(isFlying())
		{
			if(!getWorld().isClient())
				stopFlying();
			return false;
		}
		return !isFlying() && super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
	}
	
	// Performs the effect of Frost Walker on lava when the chair item also has Flame
	protected static void freezeLava(LivingEntity entity, World world, BlockPos blockPos, int level)
	{
		if(!entity.isOnGround())
			return;
		
		BlockState frosted = WHCBlocks.FROSTED_LAVA.get().getDefaultState();
		int range = Math.min(16, 2 + level);
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for(BlockPos pos : BlockPos.iterate(blockPos.add(-range, -1, -range), blockPos.add(range, -1, range)))
		{
			if(!pos.isWithinDistance(entity.getPos(), (double)range))
				continue;
			
			mutable.set(pos.getX(), pos.getY() + 1, pos.getZ());
			BlockState stateAbove = world.getBlockState(mutable);
			
			// If there is a non-air block above or the block is not the melted form of frosted lava, ignore it
			if(!stateAbove.isAir() || world.getBlockState(pos) != BlockFrostedLava.getMeltedState() || !frosted.canPlaceAt(world, pos) || !world.canPlace(frosted, pos, ShapeContext.absent()))
				continue;
			
			world.setBlockState(pos, frosted);
			world.scheduleBlockTick(pos, WHCBlocks.FROSTED_LAVA.get(), MathHelper.nextInt(entity.getRandom(), 60, 120));
		}
	}
	
	private static float addSpin(float initial, float forwardSpeed)
	{
		if(forwardSpeed == 0F)
			return initial;
		
		float amount = 360F / (float)(forwardSpeed / Math.PI);
		return clampRotation(initial + amount);
	}
	
	private static float clampRotation(float value)
	{
		if(value > 0F)
			value %= 360F;
		else
			while(value < 0F)
				value += 360F;
		
		return value;
	}
	
	protected void tickExhaustion(double deltaX, double deltaZ)
	{
		if(getWorld().isClient() || !hasPassengers()) return;
		
		PlayerEntity rider = getControllingPassenger() == null || getControllingPassenger().getType() != EntityType.PLAYER ? null : (PlayerEntity)getControllingPassenger();
		if(rider == null || !isManual(rider))
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
	
	public boolean canSprintAsVehicle()
	{
		return !hasUpgrade(WHCUpgrades.POWERED.get()) && getControllingPassenger() != null && getControllingPassenger().getType() == EntityType.PLAYER;
	}
	
	public int getEnchantmentLevel(Enchantment ench)
	{
		return EnchantmentHelper.getLevel(ench, getDataTracker().get(CHAIR));
	}
	
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor)
	{
		return new Vector3f(0F, dimensions.height * 0.85F * scaleFactor, 0F);
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
		return slot == EquipmentSlot.FEET ? getEnchantments(getChair()) : ItemStack.EMPTY;
	}
	
	public static ItemStack getEnchantments(ItemStack chair)
	{
		ItemStack spoof = Items.STONE.getDefaultStack();
		EnchantmentHelper.get(chair).forEach((enchant, lvl) -> spoof.addEnchantment(enchant, lvl));
		return spoof;
	}
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public ItemStack getChair()
	{
		ItemStack stack = getDataTracker().get(CHAIR);
		return stack.getItem() instanceof ItemWheelchair ? stack : new ItemStack(WHCItems.WHEELCHAIR_OAK.get());
	}
	
	public boolean hasColor() { return getDataTracker().get(COLOR).isPresent(); }
	public int getColor() { return getDataTracker().get(COLOR).orElse(-1); }
	
	public ItemStack getWheel(Arm arm) { return arm == Arm.LEFT ? getLeftWheel() : getRightWheel(); }
	protected ItemStack getWheel(ItemStack actualWheel)
	{
		ItemStack wheel = actualWheel.getItem().getDefaultStack().copy();
		EnchantmentHelper.get(getChair()).forEach((enchant, lvl) -> wheel.addEnchantment(enchant, lvl));
		return wheel;
	}
	public ItemStack getLeftWheel() { return getWheel(getDataTracker().get(LEFT_WHEEL)); }
	public ItemStack getRightWheel() { return getWheel(getDataTracker().get(RIGHT_WHEEL)); }
	
	public boolean consumeOnAStickItem() { return this.saddledComponent.boost(this.getRandom()); }
	
	public void setJumpStrength(int strength)
	{
		if(strength < 0)
			strength = 0;
		
		this.jumpStrength = strength > 0 ? 1F : 0F;
	}
	
	public boolean canJump() { return hasUpgrade(WHCUpgrades.DIVING.get()) && isSubmergedIn(FluidTags.WATER) || canStartFlying(); }
	
	public void startJumping(int var1) { }
	
	public void stopJumping() { }
	
	public boolean canFly() { return hasUpgrade(WHCUpgrades.GLIDING.get()); }
	
	public boolean isFlying() { return getDataTracker().get(FLYING); }
	
	public boolean isFallFlying() { return isFlying() || super.isFallFlying(); }
	
	public boolean canUseRocket() { return true; }
	
	public void startFlying()
	{
		getDataTracker().set(FLYING, true);
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, getSoundVolume(), getSoundPitch());
	}
	
	public void stopFlying()
	{
		getDataTracker().set(FLYING, false);
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, getSoundVolume(), getSoundPitch() * 0.5F);
	}
}
