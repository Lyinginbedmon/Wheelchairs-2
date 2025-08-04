package com.lying.entity;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.block.FrostedLavaBlock;
import com.lying.component.type.UpgradesComponent;
import com.lying.component.type.WheelComponent;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCChairUpgrades;
import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCEnchantments;
import com.lying.init.WHCItems;
import com.lying.item.WheelchairItem;
import com.lying.mixin.AccessorEntity;
import com.lying.reference.Reference;
import com.lying.utility.ServerEvents;
import com.lying.utility.WHCUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.entity.ReplaceDiskEnchantmentEffect;
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
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WheelchairEntity extends WheelchairsRideable implements JumpingMount, ItemSteerable, IFlyingMount, IParentedEntity
{
	private static final int REBIND_COOLDOWN = Reference.Values.TICKS_PER_SECOND * 3;
	public static final TrackedDataHandler<List<ChairUpgrade>> UPGRADE_LIST	= TrackedDataHandler.create(ChairUpgrade.PACKET_CODEC.collect(PacketCodecs.toList()));
	
	public static final TrackedData<ItemStack> CHAIR				= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	public static final TrackedData<OptionalInt> COLOR				= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<ItemStack> LEFT_WHEEL			= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	public static final TrackedData<ItemStack> RIGHT_WHEEL			= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	public static final TrackedData<List<ChairUpgrade>> UPGRADES	= DataTracker.registerData(WheelchairEntity.class, UPGRADE_LIST);
	public static final TrackedData<Boolean> POWERED				= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Optional<UUID>> USER_ID			= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<Integer> REBIND					= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Boolean> FLYING					= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> BOOST_TIME				= DataTracker.registerData(WheelchairEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	private final SaddledComponent saddledComponent;
	protected SimpleInventory items;
	protected float jumpStrength = 0F;
	private LivingEntity user = null;
	public float spinLeft, spinRight;
	
	public WheelchairEntity(EntityType<? extends WheelchairEntity> entityType, World world)
	{
		super(entityType, world);
		this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, POWERED);
		this.onChestedStatusChanged();
	}
	
	public void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		
		builder.add(CHAIR, WHCItems.WHEELCHAIR_OAK.get().getDefaultStack());
		builder.add(COLOR, OptionalInt.of(DyedColorComponent.DEFAULT_COLOR));
		builder.add(LEFT_WHEEL, WheelComponent.DEFAULT_WHEEL.get());
		builder.add(RIGHT_WHEEL, WheelComponent.DEFAULT_WHEEL.get());
		
		builder.add(UPGRADES, Lists.newArrayList());
		builder.add(POWERED, false);
		builder.add(USER_ID, Optional.empty());
		builder.add(REBIND, 0);
		builder.add(FLYING, false);
		builder.add(BOOST_TIME, 0);
	}
	
	public static DefaultAttributeContainer.Builder createWheelchairAttributes()
	{
		return createMountAttributes().add(EntityAttributes.STEP_HEIGHT, 1.0F);
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
			getDataTracker().set(CHAIR, ItemStack.fromNbt(getRegistryManager(), data.getCompound("Chair")).get());
		
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		
		if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound wheels = data.getCompound("Wheels");
			getDataTracker().set(LEFT_WHEEL, ItemStack.fromNbt(getRegistryManager(), wheels.getCompound("Left")).get());
			getDataTracker().set(RIGHT_WHEEL, ItemStack.fromNbt(getRegistryManager(), wheels.getCompound("Right")).get());
		}
		
		if(data.contains("Upgrades"))
			setUpgrades(ChairUpgrade.decodeList(NbtOps.INSTANCE, data.get("Upgrades")));
		
		onChestedStatusChanged();
		if(hasUpgrade(WHCChairUpgrades.STORAGE.get()))
		{
			NbtList items = data.getList("Items", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<items.size(); ++i)
			{
				NbtCompound nbt = items.getCompound(i);
				int j = nbt.getByte("Slot") & 0xFF;
				if (j < this.items.size())
					this.items.setStack(j, ItemStack.fromNbt(getRegistryManager(), nbt).orElse(ItemStack.EMPTY));
			}
		}
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.put("Chair", getDataTracker().get(CHAIR).toNbt(getRegistryManager()));
		if(getDataTracker().get(COLOR).isPresent())
			data.putInt("Color", getColor());
		NbtCompound wheels = new NbtCompound();
			wheels.put("Left", getLeftWheel().toNbt(getRegistryManager()));
			wheels.put("Right", getRightWheel().toNbt(getRegistryManager()));
		data.put("Wheels", wheels);
		
		if(!getUpgrades().isEmpty())
		{
			data.put("Upgrades", ChairUpgrade.encodeList(NbtOps.INSTANCE, getUpgrades()));
			
			if(hasUpgrade(WHCChairUpgrades.STORAGE.get()))
			{
				NbtList items = new NbtList();
				for(int i=0; i<this.items.size(); ++i)
				{
					ItemStack stack = this.items.getStack(i);
					if(stack.isEmpty())
						continue;
					
					NbtCompound nbt = new NbtCompound();
					nbt.putByte("Slot", (byte)i);
					items.add(stack.toNbt(getRegistryManager(), nbt));
				}
				data.put("Items", items);
			}
		}
	}
	
	public List<ChairUpgrade> getUpgrades()
	{
		return Lists.newArrayList(getDataTracker().get(UPGRADES));
	}
	
	public boolean hasUpgrade(ChairUpgrade upgrade)
	{
		return getUpgrades().stream().anyMatch(u -> u.equals(upgrade));
	}
	
	protected void setUpgrades(List<ChairUpgrade> newSet)
	{
		List<ChairUpgrade> oldSet = getUpgrades();
		List<ChairUpgrade> upgrades = newSet.stream().filter(Predicates.notNull()).toList();
		
		// Remove any upgrades currently applied that aren't in the new set
		oldSet.stream().filter(u -> !upgrades.contains(u)).forEach(u -> u.removeFrom(this));
		
		// Apply any immediate effects of upgrades that aren't in the old set
		upgrades.stream().filter(u -> !oldSet.contains(u)).forEach(u -> u.applyTo(this));
		
		getDataTracker().set(UPGRADES, upgrades);
	}
	
	public void addUpgrade(ChairUpgrade upgrade)
	{
		if(hasUpgrade(upgrade))
			return;
		
		List<ChairUpgrade> upgrades = getUpgrades();
		upgrades.add(upgrade);
		setUpgrades(upgrades);
		onChestedStatusChanged();
		
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), getSoundVolume(), getSoundPitch());
	}
	
	public void removeUpgrade(ChairUpgrade upgrade)
	{
		if(!hasUpgrade(upgrade) || hasPassengers())
			return;
		
		List<ChairUpgrade> upgrades = getUpgrades();
		upgrades.removeIf(u -> u.equals(upgrade));
		setUpgrades(upgrades);
		onChestedStatusChanged();
		
		dropItem(upgrade.dropItem());
		
		playSound(SoundEvents.ITEM_AXE_STRIP, getSoundVolume(), getSoundPitch());
	}
	
	/** Returns true if this wheelchair has the Storage upgrade */
	public boolean hasInventory() { return hasUpgrade(WHCChairUpgrades.STORAGE.get()) || hasUpgrade(WHCChairUpgrades.PLACER.get()); }
	
	public Inventory getInventory() { return this.items; }
	
	protected void onChestedStatusChanged()
	{
		if(!hasInventory() && !getWorld().isClient())
			dropInventory((ServerWorld)getWorld());
		
		SimpleInventory inv = this.items;
		this.items = new SimpleInventory(16);
		if(inv != null)
		{
			ItemStack stackInPlacer = inv.getStack(0);
			if(!stackInPlacer.isEmpty())
			{
				if(hasUpgrade(WHCChairUpgrades.PLACER.get()))
					items.setStack(0, stackInPlacer.copy());
				else if(!EnchantmentHelper.hasAnyEnchantmentsWith(stackInPlacer, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP))
					dropStack(stackInPlacer);
			}
			
			for(int i=1; i<inv.size(); i++)
			{
				ItemStack stack = inv.getStack(i);
				if(!stack.isEmpty())
					if(hasUpgrade(WHCChairUpgrades.STORAGE.get()))
						items.setStack(i, stack.copy());
					else if(!EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP))
						dropStack(stack);
			}
		}
	}
	
	public void dropInventory(ServerWorld world)
	{
		super.dropInventory(world);
		if(this.items != null)
			for(int i=0; i<this.items.size(); ++i)
			{
				ItemStack stack = this.items.getStack(i);
				if(stack.isEmpty() || EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) continue;
				this.dropStack(world, stack);
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
				// get the last upgrade and remove
				List<ChairUpgrade> upgrades = getUpgrades();
				if(upgrades.isEmpty())
					return ActionResult.FAIL;
				
				removeUpgrade(upgrades.get(upgrades.size() - 1));
				if(!player.isCreative())
					heldStack.damage(1, player);
				return ActionResult.SUCCESS_SERVER.withNewHandStack(heldStack);
			}
			else
			{
				List<ChairUpgrade> possibleUpgrades = Lists.newArrayList();
				possibleUpgrades.addAll(WHCChairUpgrades.fromItem(heldStack, this));
				if(!possibleUpgrades.isEmpty())
				{
					addUpgrade(possibleUpgrades.stream().findFirst().get());
					if(!player.getAbilities().creativeMode)
						heldStack.decrement(1);
					return ActionResult.SUCCESS_SERVER.withNewHandStack(heldStack);
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
			if(hasPassengers() && !hasPassenger(player) && hasUpgrade(WHCChairUpgrades.HANDLES.get()) && rebindCooldown() <= 0)
				return IParentedEntity.bindToPlayer(player, this) ? ActionResult.CONSUME : ActionResult.PASS;
			else if(!hasPassengers())
				return putPlayerInSaddle(player) ? ActionResult.CONSUME : ActionResult.PASS;
		}
		
		return ActionResult.SUCCESS;
	}
	
	public <T extends WheelchairsRideable> ItemStack entityToItem(T entity)
	{
		WheelchairEntity chair = (WheelchairEntity)entity;
		ItemStack stack = chair.getChair();
		WheelchairItem.setWheels(stack, chair.getLeftWheel(), chair.getRightWheel());
		if(chair.hasColor() && stack.getItem() instanceof WheelchairItem)
			stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(chair.getColor(), true));
		
		stack.set(WHCDataComponentTypes.UPGRADES.get(), UpgradesComponent.fromList(chair.getUpgrades()));
		
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		stack.getComponents().contains(DataComponentTypes.DYED_COLOR);
		getDataTracker().set(CHAIR, stack.copy());
		getDataTracker().set(COLOR, stack.getComponents().contains(DataComponentTypes.DYED_COLOR) ? OptionalInt.of(DyedColorComponent.getColor(stack, -1)) : OptionalInt.empty());
		getDataTracker().set(LEFT_WHEEL, WheelchairItem.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, WheelchairItem.getWheel(stack, Arm.RIGHT));
		
		List<ChairUpgrade> upgrades;
		if(stack.contains(WHCDataComponentTypes.UPGRADES.get()) && !(upgrades = stack.get(WHCDataComponentTypes.UPGRADES.get()).asList()).isEmpty())
			setUpgrades(upgrades);
	}
	
	/** Converts this wheelchair into an ItemEntity or (if a player is supplied) an ItemStack in a player's inventory */
	public void convertToItem(@Nullable PlayerEntity player)
	{
		if(!getWorld().isClient())
		{
			ItemStack stack = entityToItem(this);
			ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
			dropInventory((ServerWorld)getWorld());
			
			if(player == null || !player.getInventory().insertStack(stack))
				getWorld().spawnEntity(item);
			discard();
		}
	}
	
	public LivingEntity getControllingPassenger()
	{
		return !hasParent() && getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : null;
	}
	
	protected float getOffGroundSpeed() { return this.getMovementSpeed() * 0.1F; }
	
	public void tickMovement()
	{
		LivingEntity rider = null;
		if(isGliding() && (rider = (LivingEntity)getFirstPassenger()) != null)
			orientToRider(rider, Vec3d.ZERO);
		super.tickMovement();
	}
	
	/** Returns true if the wheelchair is under manual control ie. not using a chair controller */
	public boolean isManual(PlayerEntity controllingPlayer) { return !isAutomatic(controllingPlayer); }
	
	/** Returns true if the wheelchair is under automatic control ie. using a chair controller*/
	public boolean isAutomatic(PlayerEntity controllingPlayer) { return hasUpgrade(WHCChairUpgrades.POWERED.get()) && controllingPlayer.isHolding(WHCItems.CONTROLLER.get()); }
	
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
		
		if(hasUpgrade(WHCChairUpgrades.DIVING.get()) && isSubmergedIn(FluidTags.WATER))
			getWorld().addParticle(ParticleTypes.BUBBLE, getX(), getY() + 1.5D, getZ(), 0.0, 0.0, 0.0);
	}
	
	private void serverTick()
	{
		if(hasControllingPassenger() && isOnGround() && !isGliding() && age%5 == 0 && hasUpgrade(WHCChairUpgrades.PLACER.get()))
		{
			Inventory inv = getInventory();
			ItemStack stack = inv.getStack(0);
            tryPlaceBlock(stack);
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
        if(world.getBlockState(blockPos).getBlock() == item.getBlock())
        	return false;
        
        return item.place(new AutomaticItemPlacementContext(world, blockPos, direction, stack, direction)).isAccepted();
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
		ItemEnchantmentsComponent comp = chair.get(DataComponentTypes.ENCHANTMENTS);
		if(comp != null && !comp.isEmpty())
		{
			if(!controllingPlayer.isOnFire())
				WHCEnchantments.getFireProtection(getRegistryManager()).ifPresent(f -> 
				{
					int fireProtectionLevel = comp.getLevel(f);
					if(fireProtectionLevel > 0)
						controllingPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 5 * Reference.Values.TICKS_PER_SECOND * fireProtectionLevel, 0, false, false, true));
				});
			
			if(!isSubmergedIn(FluidTags.WATER))
				WHCEnchantments.getRespiration(getRegistryManager()).ifPresent(r -> 
				{
					int respirationLevel = comp.getLevel(r);
					if(respirationLevel > 0)
						controllingPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 5 * Reference.Values.TICKS_PER_SECOND * respirationLevel, 0, false, false, true));
				});
		}
		
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
		if(movementInput.length() > 0 || !hasUpgrade(WHCChairUpgrades.POWERED.get()))
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
	
	protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater)
	{
		super.updatePassengerPosition(passenger, positionUpdater);
		if(passenger instanceof LivingEntity)
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
		
		if(isGliding()) return;
		this.spinLeft = WHCUtils.wrapDegrees(this.spinLeft + amount);
		this.spinRight = WHCUtils.wrapDegrees(this.spinRight - amount);
	}
	
	public boolean isClimbing() { return super.isClimbing() && !hasUpgrade(WHCChairUpgrades.POWERED.get()); }
	
	public boolean isSaddled() { return true; }
	
	protected Vec2f getControlledRotation(LivingEntity controllingPassenger)
	{
		return new Vec2f(controllingPassenger.getPitch(), controllingPassenger.getYaw());
	}
	
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		if(isGliding())
			return getVelocity();
		
		double modifier = 1D;
		if(!isOnGround() && !hasUpgrade(WHCChairUpgrades.GLIDING.get()))
			if(shouldBobUp())
				modifier = 0.9D;
			else
				modifier = 0.7D;
		
		Vec3d speed = isAutomatic(controllingPlayer) ? new Vec3d(0, 0, 1D) : new Vec3d(0, 0, controllingPlayer.forwardSpeed);
		return speed.multiply(modifier);
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * (isManual(controllingPlayer) ? 1F : this.saddledComponent.getMovementSpeedMultiplier());
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
		
		if(getWorld().isClient() && !isGliding())
		{
			Vec3d local = WHCUtils.globalToLocal(movementInput, getYaw());
			double speed = WHCUtils.calculateSpin((float)local.getZ(), 1F);
			this.spinLeft = WHCUtils.wrapDegrees(this.spinLeft + (float)speed);
			this.spinRight = WHCUtils.wrapDegrees(this.spinRight + (float)speed);
		}
	}
	
	public float getActualStepHeight() { return 1F; }
	
	public void travelControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		if(shouldBobUp())
			addVelocity(0D, 0.08D, 0D);
		
		super.travel(movementInput);
	}
	
	protected boolean shouldBobUp()
	{
		double swimHeight = getSwimHeight();
		return hasUpgrade(WHCChairUpgrades.FLOATING.get()) &&
				(getFluidHeight(FluidTags.WATER) > swimHeight || 
				(hasUpgrade(WHCChairUpgrades.NETHERITE.get()) && getFluidHeight(FluidTags.LAVA) > swimHeight));
	}
	
	@SuppressWarnings("resource")
	public void applyMovementEffects(ServerWorld world, BlockPos pos)
	{
		super.applyMovementEffects(world, pos);
		ItemStack chair = getChair();
		ItemEnchantmentsComponent comp = chair.get(DataComponentTypes.ENCHANTMENTS);
		if(comp == null || comp.isEmpty())
			return;
		
		if(isOnGround() && hasUpgrade(WHCChairUpgrades.NETHERITE.get()))
		{
			List<RegistryEntry<Enchantment>> frostWalkers = comp.getEnchantments().stream().filter(ench -> 
			{
				ComponentMap effects = ench.value().effects();
				if(!effects.contains(EnchantmentEffectComponentTypes.LOCATION_CHANGED))
					return false;
				
				return effects.get(EnchantmentEffectComponentTypes.LOCATION_CHANGED).stream()
					.filter(e -> e.effect() instanceof ReplaceDiskEnchantmentEffect && ((ReplaceDiskEnchantmentEffect)e.effect()).blockState().get(getRandom(), getBlockPos()).isOf(Blocks.FROSTED_ICE))
					.findFirst().isPresent();
			}).toList();
			
			frostWalkers.stream().map(e -> EnchantmentHelper.getLevel(e, chair)).sorted().findFirst().ifPresent(level -> freezeLava(this, getWorld(), getBlockPos(), level));
		}
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
			if(!stateAbove.isAir() || world.getBlockState(pos) != FrostedLavaBlock.getMeltedState() || !frosted.canPlaceAt(world, pos) || !world.canPlace(frosted, pos, ShapeContext.absent()))
				continue;
			
			world.setBlockState(pos, frosted);
			world.scheduleBlockTick(pos, WHCBlocks.FROSTED_LAVA.get(), MathHelper.nextInt(entity.getRandom(), 60, 120));
		}
	}
	
	public boolean canSprintAsVehicle()
	{
		return !hasUpgrade(WHCChairUpgrades.POWERED.get()) && getControllingPassenger() != null && getControllingPassenger().getType() == EntityType.PLAYER;
	}
	
	public int getEnchantmentLevel(RegistryEntry<Enchantment> ench)
	{
		return EnchantmentHelper.getEnchantments(getDataTracker().get(CHAIR)).getLevel(ench);
	}
	
	protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor)
	{
		return new Vec3d(0F, dimensions.height() * 0.85F * scaleFactor, 0F);
	}
	
	public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect)
	{
		if(hasPassengers() && getControllingPassenger() instanceof LivingEntity)
			return getControllingPassenger().hasStatusEffect(effect);
		return false;
	}
	
	public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect)
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
		spoof.set(DataComponentTypes.ENCHANTMENTS, chair.get(DataComponentTypes.ENCHANTMENTS));
		return spoof;
	}
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public ItemStack getChair()
	{
		ItemStack stack = getDataTracker().get(CHAIR);
		return stack.getItem() instanceof WheelchairItem ? stack : new ItemStack(WHCItems.WHEELCHAIR_OAK);
	}
	
	public boolean hasColor() { return getDataTracker().get(COLOR).isPresent(); }
	public int getColor() { return getDataTracker().get(COLOR).orElse(-1); }
	
	public ItemStack getWheel(Arm arm) { return arm == Arm.LEFT ? getLeftWheel() : getRightWheel(); }
	protected ItemStack getWheel(ItemStack actualWheel)
	{
		ItemStack wheel = actualWheel.getItem().getDefaultStack().copy();
		wheel.set(DataComponentTypes.ENCHANTMENTS, getChair().get(DataComponentTypes.ENCHANTMENTS));
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
	
	public boolean canJump() { return hasUpgrade(WHCChairUpgrades.DIVING.get()) && isSubmergedIn(FluidTags.WATER) || canStartFlying(); }
	
	public void startJumping(int var1) { }
	
	public void stopJumping() { }
	
	public boolean canFly() { return hasUpgrade(WHCChairUpgrades.GLIDING.get()); }
	
	public boolean isFlying() { return getDataTracker().get(FLYING); }
	
	public boolean isGliding() { return isFlying() || super.isGliding(); }
	
	public boolean canUseRocket() { return true; }
	
	public void startFlying()
	{
		getDataTracker().set(FLYING, true);
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), getSoundVolume(), getSoundPitch());
	}
	
	public void stopFlying()
	{
		getDataTracker().set(FLYING, false);
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), getSoundVolume(), getSoundPitch() * 0.5F);
	}
	
	public boolean hasParent() { return hasUpgrade(WHCChairUpgrades.HANDLES.get()) && getDataTracker().get(USER_ID).isPresent(); }
	
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
		if(!hasUpgrade(WHCChairUpgrades.HANDLES.get()) || hasPassenger(parent))
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
