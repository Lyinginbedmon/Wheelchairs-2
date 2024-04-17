package com.lying.wheelchairs.entity;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCUpgrades;
import com.lying.wheelchairs.item.ItemWheelchair;

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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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

	private static final TrackedData<NbtCompound> UPGRADES = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	public static final TrackedData<Boolean> POWERED = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
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
		
		this.getDataTracker().startTracking(UPGRADES, new NbtCompound());
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
		
		if(data.contains("Upgrades", NbtElement.LIST_TYPE))
			setUpgrades(data.getList("Upgrades", NbtElement.STRING_TYPE));
		
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
		
		data.put("Upgrades", getUpgradeList());
		this.saddledComponent.writeNbt(data);
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
	
	public boolean hasUpgrade(ChairUpgrade upgrade)
	{
		return getUpgrades().contains(upgrade);
	}
	
	public void addUpgrade(ChairUpgrade upgrade)
	{
		if(hasUpgrade(upgrade))
			return;
		
		NbtList upgrades = getUpgradeList();
		upgrades.add(NbtString.of(upgrade.registryName().toString()));
		setUpgrades(upgrades);
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getStackInHand(hand);
		if(player.shouldCancelInteraction())
		{
			List<ChairUpgrade> possibleUpgrades = Lists.newArrayList();
			WHCUpgrades.fromItem(heldStack).forEach(upgr -> 
			{
				if(!hasUpgrade(upgr))
					possibleUpgrades.add(upgr);
			});
			
			if(!possibleUpgrades.isEmpty())
			{
				addUpgrade(possibleUpgrades.toArray(new ChairUpgrade[0])[0]);
				if(!player.getAbilities().creativeMode)
					heldStack.decrement(1);
				return ActionResult.success(getWorld().isClient());
			}
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
		
		stack.getNbt().put("Upgrades", chair.getUpgradeList());
		
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
			setUpgrades(stackData.getList("Upgrades", NbtElement.STRING_TYPE));
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
	
	public boolean canStartRiding(Entity entity) { return false; }
	
	/** Returns true if the wheelchair is under manual control ie. not using a wheelchair controller */
	public boolean isManual(PlayerEntity controllingPlayer) { return !(hasUpgrade(WHCUpgrades.POWERED) && controllingPlayer.isHolding(WHCItems.CONTROLLER)); }
	
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		super.tickControlled(controllingPlayer, movementMultiplier);
		if(!isManual(controllingPlayer) && isSprinting())
		{
			setSprinting(false);
			controllingPlayer.setSprinting(false);
		}
		
		Vec2f orientation = getControlledRotation(controllingPlayer);
		if(movementInput.length() > 0 || !hasUpgrade(WHCUpgrades.POWERED))
		{
			this.setRotation(orientation.y, orientation.x);
			this.prevYaw = this.headYaw;
			this.bodyYaw = this.headYaw = this.getYaw();
		}
		
		this.saddledComponent.tickBoost();
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
	
	public void move(MovementType type, Vec3d movementInput)
	{
		double x = getX();
		double z = getZ();
		super.move(type, movementInput);
		this.tickExhaustion(getX() - x, getZ() - z);
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
		LivingEntity player = getControllingPassenger();
		return player.getType() == EntityType.PLAYER && !hasUpgrade(WHCUpgrades.POWERED);
	}
	
	public int getEnchantmentLevel(Enchantment ench)
	{
		return Math.max(EnchantmentHelper.getLevel(ench, getWheel(Arm.LEFT)), EnchantmentHelper.getLevel(ench, getWheel(Arm.RIGHT)));
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
}
