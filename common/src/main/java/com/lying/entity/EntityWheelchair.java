package com.lying.entity;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.lying.block.BlockFrostedLava;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCItems;
import com.lying.init.WHCUpgrades;
import com.lying.item.ItemWheelchair;
import com.lying.mixin.AccessorEntity;
import com.lying.reference.Reference;
import com.lying.utility.ServerEvents;
import com.lying.utility.WHCUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWheelchair extends WheelchairsRideable implements JumpingMount, ItemSteerable, IFlyingMount, IParentedEntity
{
	private static final int REBIND_COOLDOWN = Reference.Values.TICKS_PER_SECOND * 3;
	
	public static final TrackedData<ItemStack> CHAIR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	public static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	public static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	public static final TrackedData<NbtCompound> UPGRADES = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	public static final TrackedData<Boolean> POWERED = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Optional<UUID>> USER_ID = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<Integer> REBIND = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Boolean> FLYING = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.INTEGER);
	private final SaddledComponent saddledComponent;
	
	protected SimpleInventory items;
	protected float jumpStrength = 0F;
	private LivingEntity user = null;
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
		this.getDataTracker().startTracking(USER_ID, Optional.empty());
		this.getDataTracker().startTracking(REBIND, 0);
		this.getDataTracker().startTracking(FLYING, false);
		this.getDataTracker().startTracking(BOOST_TIME, 0);
	}
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(BOOST_TIME.equals(data) && getWorld().isClient())
			this.saddledComponent.boost();
		super.onTrackedDataSet(data);
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
		
		onChestedStatusChanged();
		if(hasUpgrade(WHCUpgrades.STORAGE.get()))
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
		if(hasUpgrade(WHCUpgrades.STORAGE.get()))
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
	
	public boolean hasUpgrade(ChairUpgrade upgrade)
	{
		return getUpgradeList().stream().anyMatch(element -> element.asString().equals(upgrade.registryName().toString()));
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
	
	public void removeUpgrade(ChairUpgrade upgrade)
	{
		if(!hasUpgrade(upgrade) || hasPassengers())
			return;
		
		NbtList upgrades = getUpgradeList();
		upgrades.removeIf(element -> element.asString().equals(upgrade.registryName().toString()));
		setUpgrades(upgrades);
		onChestedStatusChanged();
		
		dropItem(upgrade.dropItem());
		
		playSound(SoundEvents.ITEM_AXE_STRIP, getSoundVolume(), getSoundPitch());
	}
	
	/** Returns true if this wheelchair has the Storage upgrade */
	public boolean hasInventory() { return hasUpgrade(WHCUpgrades.STORAGE.get()) || hasUpgrade(WHCUpgrades.PLACER.get()); }
	
	public Inventory getInventory() { return this.items; }
	
	protected void onChestedStatusChanged()
	{
		if(!hasInventory())
			dropInventory();
		
		SimpleInventory inv = this.items;
		this.items = new SimpleInventory(16);
		if(inv != null)
		{
			ItemStack stackInPlacer = inv.getStack(0);
			if(!stackInPlacer.isEmpty())
			{
				if(hasUpgrade(WHCUpgrades.PLACER.get()))
					items.setStack(0, stackInPlacer.copy());
				else if(!EnchantmentHelper.hasVanishingCurse(stackInPlacer))
					dropStack(stackInPlacer);
			}
			
			for(int i=1; i<inv.size(); i++)
			{
				ItemStack stack = inv.getStack(i);
				if(!stack.isEmpty())
					if(hasUpgrade(WHCUpgrades.STORAGE.get()))
						items.setStack(i, stack.copy());
					else if(!EnchantmentHelper.hasVanishingCurse(stack))
						dropStack(stack);
			}
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
		if(player.shouldCancelInteraction() && !hasPassengers() && !hasParent())
		{
			if(heldStack.isIn(ItemTags.AXES))
			{
				// Get the last upgrade and remove
				List<ChairUpgrade> upgrades = getUpgrades();
				if(upgrades.isEmpty())
					return ActionResult.FAIL;
				removeUpgrade(upgrades.get(upgrades.size() - 1));
				if(!player.isCreative())
					heldStack.damage(1, player, playerx -> playerx.sendToolBreakStatus(hand));
				return ActionResult.success(getWorld().isClient());
			}
			else
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
		}
		
		if(player.shouldCancelInteraction() && !hasPassengers() && !hasParent())
		{
			convertToItem(null);
			return ActionResult.CONSUME;
		}
		else if(!this.getWorld().isClient())
		{
			if(hasPassengers() && hasUpgrade(WHCUpgrades.HANDLES.get()) && rebindCooldown() <= 0)
				return IParentedEntity.bindToPlayer(player, this) ? ActionResult.CONSUME : ActionResult.PASS;
			else if(!hasPassengers())
				return putPlayerInSaddle(player) ? ActionResult.CONSUME : ActionResult.PASS;
		}
		
		return ActionResult.SUCCESS;
	}
	
	public <T extends WheelchairsRideable> ItemStack entityToItem(T entity)
	{
		EntityWheelchair chair = (EntityWheelchair)entity;
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
			ItemStack stack = entityToItem(this);
			ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
			dropInventory();
			
			if(player == null || !player.getInventory().insertStack(stack))
				getWorld().spawnEntity(item);
			discard();
		}
	}
	
	public LivingEntity getControllingPassenger()
	{
		return !isFallFlying() && !hasParent() && getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : null;
	}
	
	public void tickMovement()
	{
		LivingEntity rider = null;
		if(isFallFlying() && (rider = (LivingEntity)getFirstPassenger()) != null)
			orientToRider(rider, Vec3d.ZERO);
		super.tickMovement();
	}
	
	/** Returns true if the wheelchair is under manual control ie. not using a chair controller */
	public boolean isManual(PlayerEntity controllingPlayer) { return !isAutomatic(controllingPlayer); }
	
	/** Returns true if the wheelchair is under automatic control ie. using a chair controller*/
	public boolean isAutomatic(PlayerEntity controllingPlayer) { return hasUpgrade(WHCUpgrades.POWERED.get()) && controllingPlayer.isHolding(WHCItems.CONTROLLER.get()); }
	
	public boolean isSneaking() { return super.isSneaking() || hasPassengers() && getFirstPassenger() instanceof LivingEntity && getFirstPassenger().isSneaking(); }
	
	public void tick()
	{
		super.tick();
		if(getWorld().isClient())
			clientTick();
		else
			serverTick();
	}
	
	private void clientTick()
	{
		if(this.saddledComponent.getMovementSpeedMultiplier() > 1F)
			getWorld().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0.0, 0.0, 0.0);
		
		if(hasUpgrade(WHCUpgrades.DIVING.get()) && isSubmergedIn(FluidTags.WATER))
			getWorld().addParticle(ParticleTypes.BUBBLE, getX(), getY() + 1.5D, getZ(), 0.0, 0.0, 0.0);
	}
	
	private void serverTick()
	{
		if(hasControllingPassenger() && isOnGround() && !isFallFlying() && age%5 == 0 && hasUpgrade(WHCUpgrades.PLACER.get()))
		{
			Inventory inv = getInventory();
			ItemStack stack = inv.getStack(0);
            if(tryPlaceBlock(stack))
            	stack.decrement(1);
		}
		
		if(hasParent() && getParent() == null)
			parentTo(null);
		
		if(rebindCooldown() > 0)
			getDataTracker().set(REBIND, rebindCooldown() - 1);
	}
	
	private boolean tryPlaceBlock(ItemStack stack)
	{
		if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem) || getWorld().isClient())
			return false;
		
		BlockItem item = (BlockItem)stack.getItem();
        ServerWorld world = (ServerWorld)getWorld();
        Direction direction = Direction.DOWN;
        BlockPos blockPos = getBlockPos().offset(direction);
        Direction direction2 = world.isAir(getBlockPos().down()) ? direction : Direction.UP;
        if(blockPos.getY() < world.getBottomY() + 2 || !world.isAir(blockPos) || world.getBlockState(blockPos).isOf(item.getBlock()))
        	return false;
        
        try
        {
        	return item.place(new AutomaticItemPlacementContext(world, blockPos, direction, stack.copy(), direction2)).isAccepted();
        }
        catch(Exception e) { return false; }
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
					ServerEvents.ON_DOUBLE_JUMP.invoker().onDoubleJump(this);
					startFlying();
				}
				else
					jump();
		}
		this.jumpStrength = 0F;
	}
	
	protected void orientToRider(LivingEntity controllingPlayer, Vec3d movementInput)
	{
		if(movementInput.length() > 0 || !hasUpgrade(WHCUpgrades.POWERED.get()))
			super.orientToRider(controllingPlayer, movementInput);
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
		this.spinLeft = WHCUtils.wrapDegrees(this.spinLeft + amount);
		this.spinRight = WHCUtils.wrapDegrees(this.spinRight - amount);
	}
	
	public boolean isClimbing() { return super.isClimbing() && !hasUpgrade(WHCUpgrades.POWERED.get()); }
	
	public boolean isSaddled() { return true; }
	
	protected Vec2f getControlledRotation(LivingEntity controllingPassenger)
	{
		return new Vec2f(controllingPassenger.getPitch(), controllingPassenger.getYaw());
	}
	
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		double modifier = 1D;
		if(!isOnGround() && !hasUpgrade(WHCUpgrades.GLIDING.get()))
			if(shouldBobUp())
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
			movementInput = ((AccessorEntity)rider).adjustToPreventCollision(movementInput);
		
		super.move(type, movementInput);
		this.tickExhaustion(getX() - x, getZ() - z);
		
		if(getWorld().isClient() && !isFallFlying())
		{
			Vec3d local = WHCUtils.globalToLocal(movementInput, getYaw());
			double speed = WHCUtils.calculateSpin((float)local.getZ(), 1F);
			this.spinLeft = WHCUtils.wrapDegrees(this.spinLeft + (float)speed);
			this.spinRight = WHCUtils.wrapDegrees(this.spinRight + (float)speed);
		}
	}
	
	public float getActualStepHeight() { return 1F; }
	
	public void travel(Vec3d movementInput)
	{
		if(shouldBobUp())
			addVelocity(0D, 0.08D, 0D);
		
		super.travel(movementInput);
	}
	
	protected boolean shouldBobUp()
	{
		double swimHeight = getSwimHeight();
		return hasUpgrade(WHCUpgrades.FLOATING.get()) &&
				(getFluidHeight(FluidTags.WATER) > swimHeight || 
				(hasUpgrade(WHCUpgrades.NETHERITE.get()) && getFluidHeight(FluidTags.LAVA) > swimHeight));
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
		return new Vector3f(0F, dimensions.height * 0.75F * scaleFactor, 0F);
	}
	
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
	
	public boolean hasParent() { return hasUpgrade(WHCUpgrades.HANDLES.get()) && getDataTracker().get(USER_ID).isPresent(); }
	
	public boolean isParent(Entity entity) { return hasParent() && getDataTracker().get(USER_ID).get().equals(entity.getUuid()); }
	
	@Nullable
	public LivingEntity getParent()
	{
		if(!hasParent())
			return null;
		
		return user == null ? (user = IParentedEntity.getParentOf(this)) : user;
	}
	
	public void parentTo(@Nullable LivingEntity parent)
	{
		if(!hasUpgrade(WHCUpgrades.HANDLES.get()) || hasPassenger(parent))
			return;
		
		getDataTracker().set(USER_ID, parent == null ? Optional.empty() : Optional.of(parent.getUuid()));
	}
	
	public Vec3d getParentOffset(LivingEntity parent, float yaw, float pitch)
	{
		return WHCUtils.localToGlobal(new Vec3d(0, 0, 0.75D), parent.bodyYaw);
	}
	
	public void tickParented(@NotNull LivingEntity parent, float yaw, float pitch)
	{
		setRotation(parent.bodyYaw, 0F);
		
		// Unbind from user if user is riding or holding two items
		if(!canParentToChild(parent, this))
			clearParent();
	}
	
	public void pushAway(Entity entity)
	{
		if(!isParent(entity))
			super.pushAway(entity);
	}
	
	public void pushAwayFrom(Entity entity)
	{
		if(!isParent(entity))
			super.pushAwayFrom(entity);
	}
	
	public void forceUnbind()
	{
		if(!hasParent())
			return;
		
		clearParent();
		resetRebindCooldown();
	}
	
	public int rebindCooldown() { return getDataTracker().get(REBIND).intValue(); }
	
	public void resetRebindCooldown() { getDataTracker().set(REBIND, REBIND_COOLDOWN); }
}