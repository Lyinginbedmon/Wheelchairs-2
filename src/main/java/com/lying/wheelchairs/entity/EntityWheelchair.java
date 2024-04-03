package com.lying.wheelchairs.entity;

import java.util.OptionalInt;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.item.ItemWheelchair;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWheelchair extends LivingEntity
{
	private static final TrackedData<ItemStack> CHAIR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	private static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(EntityWheelchair.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	public EntityWheelchair(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(1.0f);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		
		// FIXME Ensure values are properly initialised
		this.getDataTracker().startTracking(CHAIR, WHCItems.WHEELCHAIR_OAK.getDefaultStack());
		this.getDataTracker().startTracking(COLOR, OptionalInt.empty());
		this.getDataTracker().startTracking(LEFT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
		this.getDataTracker().startTracking(RIGHT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		getDataTracker().set(CHAIR, ItemStack.fromNbt(data.getCompound("Chair")));
		
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		
		NbtCompound wheels = data.getCompound("Wheels");
		getDataTracker().set(LEFT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Left")));
		getDataTracker().set(RIGHT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Right")));
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
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		if(!hasPassengers())
			if(player.shouldCancelInteraction())
			{
				ItemStack stack = getChair();
				ItemWheelchair.setWheels(stack, getLeftWheel(), getRightWheel());
				if(hasColor() && stack.getItem() instanceof ItemWheelchair)
					((ItemWheelchair)stack.getItem()).setColor(stack, getColor());
				
				ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
				if(!getWorld().isClient())
				{
					getWorld().spawnEntity(item);
					discard();
				}
				return ActionResult.CONSUME;
			}
			else if(!this.getWorld().isClient())
				return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		
		return ActionResult.SUCCESS;
	}
	
	public LivingEntity getControllingPassenger() { return hasPassengers() && getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : super.getControllingPassenger(); }
	
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		super.tickControlled(controllingPlayer, movementMultiplier);
		
		Vec2f orientation = getControlledRotation(controllingPlayer);
		this.setRotation(orientation.y, orientation.x);
		this.bodyYaw = this.headYaw = this.getYaw();
		this.prevYaw = this.headYaw;
	}
	
	protected Vec2f getControlledRotation(LivingEntity controllingPassenger)
	{
		return new Vec2f(controllingPassenger.getPitch(), controllingPassenger.getYaw());
	}
	
	// FIXME Ensure movement input is actually applied
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		if(!this.isOnGround())
			return Vec3d.ZERO;
		
		return new Vec3d(0D, 0D, controllingPlayer.forwardSpeed);
	}
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(4, ItemStack.EMPTY); }
	
	public boolean canEquip(ItemStack stack) { return false; }
	
	public ItemStack getEquippedStack(EquipmentSlot var1) { return ItemStack.EMPTY; }
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public void copyPartsFromItem(ItemStack stack)
	{
		getDataTracker().set(CHAIR, stack);
		getDataTracker().set(COLOR, stack.getItem() instanceof DyeableItem ? OptionalInt.of(((DyeableItem)stack.getItem()).getColor(stack)) : OptionalInt.empty());
		getDataTracker().set(LEFT_WHEEL, ItemWheelchair.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, ItemWheelchair.getWheel(stack, Arm.RIGHT));
	}
	
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
}
