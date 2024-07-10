package com.lying.wheelchairs.entity;

import java.util.OptionalInt;

import org.joml.Vector3f;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.item.ItemStool;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Mount;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityStool extends WheelchairsRideable implements Mount
{
	public static final int DEFAULT_COLOR = 1776411;
	public static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityStool.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<ItemStack> WHEELS = DataTracker.registerData(EntityStool.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	public EntityStool(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(0.6f);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		getDataTracker().startTracking(COLOR, OptionalInt.empty());
		getDataTracker().startTracking(WHEELS, new ItemStack(WHCItems.WHEEL_OAK));
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		getDataTracker().set(WHEELS, ItemStack.fromNbt(data.getCompound("Wheels")));
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.put("Wheels", getWheels().writeNbt(new NbtCompound()));
		if(hasColor())
			data.putInt("Color", getColor());
	}
	
	public <T extends WheelchairsRideable> ItemStack entityToItem(T chair)
	{
		EntityStool stool = (EntityStool)chair;
		ItemStack stack = WHCItems.STOOL.getDefaultStack().copy();
		ItemStool.setWheels(stack, stool.getWheels());
		if(stool.hasColor() && stack.getItem() instanceof DyeableItem)
			((ItemStool)stack.getItem()).setColor(stack, stool.getColor());
		
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(COLOR, OptionalInt.of(((DyeableItem)stack.getItem()).getColor(stack)));
		getDataTracker().set(WHEELS, ItemStool.getWheels(stack));
	}
	
	public LivingEntity getControllingPassenger()
	{
		return getFirstPassenger() instanceof LivingEntity ? (LivingEntity)getFirstPassenger() : null;
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}
	
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		return new Vec3d(controllingPlayer.sidewaysSpeed, 0, controllingPlayer.forwardSpeed);
	}
	
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor)
	{
		return new Vector3f(0F, dimensions.height * 0.85F * scaleFactor, 0F);
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
	
	public boolean hasColor() { return getDataTracker().get(COLOR).isPresent(); }
	
	public int getColor() { return hasColor() ? getDataTracker().get(COLOR).getAsInt() : DEFAULT_COLOR; }
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(4, ItemStack.EMPTY); }
	
	public boolean canEquip(ItemStack stack) { return false; }
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public ItemStack getEquippedStack(EquipmentSlot slot) { return ItemStack.EMPTY; }
	
	public float getActualStepHeight() { return 0.6F; }
	
	public ItemStack getWheels() { return getDataTracker().get(WHEELS).copy(); }
}
