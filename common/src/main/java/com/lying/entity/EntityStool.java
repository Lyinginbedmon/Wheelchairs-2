package com.lying.entity;

import java.util.OptionalInt;

import org.joml.Vector2d;
import org.joml.Vector3f;

import com.lying.init.WHCItems;
import com.lying.item.ItemStool;
import com.lying.utility.WHCUtils;

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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityStool extends WheelchairsRideable implements Mount
{
	public static final int DEFAULT_COLOR = ItemStool.DEFAULT_COLOR;
	public static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityStool.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	
	public float spin = 0F;
	private Vector2d prevCaster, caster;
	
	public EntityStool(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(0.6f);
		
		double randX = (getRandom().nextDouble() - 0.5D) * 2D;
		double randY = (getRandom().nextDouble() - 0.5D) * 2D;
		prevCaster = caster = new Vector2d(randX, randY);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		getDataTracker().startTracking(COLOR, OptionalInt.empty());
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		if(hasColor())
			data.putInt("Color", getColor());
	}
	
	public <T extends WheelchairsRideable> ItemStack entityToItem(T chair)
	{
		EntityStool stool = (EntityStool)chair;
		ItemStack stack = WHCItems.STOOL.get().getDefaultStack().copy();
		if(stool.hasColor() && stack.getItem() instanceof DyeableItem)
			((ItemStool)stack.getItem()).setColor(stack, stool.getColor());
		
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		DyeableItem item = (DyeableItem)stack.getItem();
		getDataTracker().set(COLOR, item.hasColor(stack) ? OptionalInt.of(item.getColor(stack)) : OptionalInt.empty());
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
	
	protected void orientToRider(LivingEntity controllingPlayer, Vec3d movementInput)
	{
		if(movementInput.length() > 0D)
		{
			Vec2f orientation = getControlledRotation(controllingPlayer);
			setRotation(orientation.y, orientation.x);
		}
		else
			setRotation(controllingPlayer.bodyYaw, 0F);
		
		this.prevYaw = this.headYaw;
		this.bodyYaw = this.headYaw = this.getYaw();
	}
	
	public void travel(Vec3d movementInput)
	{
		super.travel(movementInput);
		
		Vector2d lateral = new Vector2d(movementInput.x, movementInput.z);
		if(lateral.length() == 0D) return;
		
		double speed = WHCUtils.calculateSpin((float)(movementInput.length() * getMovementSpeed()), 5F / 16F);
		this.spin = WHCUtils.wrapDegrees(this.spin + (float)speed);
		
		Vec3d global = WHCUtils.localToGlobal(movementInput, getYaw());
		lateral = new Vector2d(global.x, global.z).mul(0.5D);
		caster.get(prevCaster);
		caster.add(lateral).normalize();
	}
	
	public float casterWheelYaw(float tickDelta)
	{
		// Values cloned because Vector2d performs all operations on the value itself instead of returning new ones
		Vector2d origin = prevCaster.get(new Vector2d());
		Vector2d current = caster.get(new Vector2d());
		origin.add(current.sub(origin).mul(tickDelta));
		return (float)Math.toDegrees(Math.atan2(origin.y, origin.x));
	}
	
    public double getMountedHeightOffset(Entity passenger)
    {
        return (double)getHeight() * (passenger.isInSneakingPose() ? 0.75 : 0.55);
    }
}
