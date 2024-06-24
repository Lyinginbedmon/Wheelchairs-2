package com.lying.wheelchairs.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCSoundEvents;
import com.lying.wheelchairs.item.ItemWalker;
import com.lying.wheelchairs.utility.ServerBus;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWalker extends LivingEntity implements IParentedEntity
{
	private static final TrackedData<ItemStack> CHAIR = DataTracker.registerData(EntityWalker.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(EntityWalker.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(EntityWalker.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Optional<UUID>> USER_ID = DataTracker.registerData(EntityWalker.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	
	private LivingEntity user = null;
	public float prevFrameYaw, frameYaw = 0F;
	public float prevCasterYaw, casterYaw = 0F;
	public float spinLeft, spinRight;
	
	public EntityWalker(EntityType<? extends EntityWalker> entityType, World world)
	{
		super(entityType, world);
		this.setStepHeight(0.5F);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		
		this.getDataTracker().startTracking(CHAIR, WHCItems.WHEELCHAIR_OAK.getDefaultStack());
		this.getDataTracker().startTracking(LEFT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
		this.getDataTracker().startTracking(RIGHT_WHEEL, new ItemStack(WHCItems.WHEEL_OAK));
		this.getDataTracker().startTracking(USER_ID, Optional.empty());
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
		
		if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound wheels = data.getCompound("Wheels");
			getDataTracker().set(LEFT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Left")));
			getDataTracker().set(RIGHT_WHEEL, ItemStack.fromNbt(wheels.getCompound("Right")));
		}
		
		if(data.contains("User", NbtElement.INT_ARRAY_TYPE))
			getDataTracker().set(USER_ID, Optional.of(data.getUuid("User")));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.put("Chair", getDataTracker().get(CHAIR).writeNbt(new NbtCompound()));
		NbtCompound wheels = new NbtCompound();
			wheels.put("Left", getLeftWheel().writeNbt(new NbtCompound()));
			wheels.put("Right", getRightWheel().writeNbt(new NbtCompound()));
		data.put("Wheels", wheels);
		
		if(hasParent())
			data.putUuid("User", getDataTracker().get(USER_ID).get());
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		if(player.shouldCancelInteraction())
		{
			this.convertToItem(null);
			return ActionResult.CONSUME;
		}
		else if(!getWorld().isClient())
			return bindToPlayer(player) ? ActionResult.CONSUME : ActionResult.PASS;
		
		return ActionResult.SUCCESS;
	}
	
	public static ItemStack chairToItem(EntityWalker chair)
	{
		ItemStack stack = chair.getFrame();
		ItemWalker.setWheels(stack, chair.getLeftWheel(), chair.getRightWheel());
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(CHAIR, stack.copy());
		getDataTracker().set(LEFT_WHEEL, ItemWalker.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, ItemWalker.getWheel(stack, Arm.RIGHT));
	}
	
	@Nullable
	public LivingEntity getUser()
	{
		if(!hasParent())
			return null;
		
		return user == null ? (user = IParentedEntity.getParentOf(this)) : user;
	}
	
	public boolean bindToPlayer(PlayerEntity player)
	{
		if(getWorld().isClient())
			return false;
		
		if(isParent(player))
		{
			parentTo(null);
			playSound(WHCSoundEvents.SEATBELT_OFF, 1F, 1F);
			return true;
		}
		else if((!hasParent() || player.isCreative()) && canUseWalker(player, this))
		{
			parentTo(player);
			playSound(WHCSoundEvents.SEATBELT_ON, 1F, 1F);
			
			ServerBus.ON_WALKER_BIND.invoker().onBindToWalker(player, this);
			return true;
		}
		
		return false;
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
	
	public static boolean canUseWalker(LivingEntity entity, Entity walker)
	{
		return !entity.hasVehicle() && (entity.getMainHandStack().isEmpty() || entity.getOffHandStack().isEmpty()) && entity.distanceTo(walker) < 5D;
	}
	
	public void pushAway(Entity entity)
	{
		if(isParent(entity))
			return;
		super.pushAway(entity);
	}
	
	public void pushAwayFrom(Entity entity)
	{
		if(isParent(entity))
			return;
		super.pushAwayFrom(entity);
	}
	
	public Vec3d getVelocity()
	{
		Entity user = getUser();
		return user == null ? super.getVelocity() : user.getVelocity();
	}
	
	public void travel(Vec3d movementInput)
	{
		super.travel(movementInput);
		
		if(isFallFlying()) return;
		double speed = movementInput.getZ() * getMovementSpeed();
		this.spinLeft = addSpin(this.spinLeft, (float)speed);
		this.spinRight = addSpin(this.spinRight, (float)speed);
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
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(4, ItemStack.EMPTY); }
	
	public boolean canEquip(ItemStack stack) { return false; }
	
	public ItemStack getEquippedStack(EquipmentSlot slot)
	{
		return slot == EquipmentSlot.FEET ? getEnchantments(getFrame()) : ItemStack.EMPTY;
	}
	
	public static ItemStack getEnchantments(ItemStack chair)
	{
		ItemStack spoof = Items.STONE.getDefaultStack();
		EnchantmentHelper.get(chair).forEach((enchant, lvl) -> spoof.addEnchantment(enchant, lvl));
		return spoof;
	}
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public ItemStack getFrame()
	{
		ItemStack stack = getDataTracker().get(CHAIR);
		return stack.getItem() instanceof ItemWalker ? stack : new ItemStack(WHCItems.WHEELCHAIR_OAK);
	}
	
	public ItemStack getWheel(Arm arm) { return arm == Arm.LEFT ? getLeftWheel() : getRightWheel(); }
	protected ItemStack getWheel(ItemStack actualWheel)
	{
		ItemStack wheel = actualWheel.getItem().getDefaultStack().copy();
		EnchantmentHelper.get(getFrame()).forEach((enchant, lvl) -> wheel.addEnchantment(enchant, lvl));
		return wheel;
	}
	public ItemStack getLeftWheel() { return getWheel(getDataTracker().get(LEFT_WHEEL)); }
	public ItemStack getRightWheel() { return getWheel(getDataTracker().get(RIGHT_WHEEL)); }
	
	public boolean hasParent() { return getDataTracker().get(USER_ID).isPresent(); }
	
	public boolean isParent(Entity entity) { return hasParent() && entity.getUuid().equals(getDataTracker().get(USER_ID).get()); }
	
	public void parentTo(@Nullable LivingEntity entity)
	{
		getDataTracker().set(USER_ID, entity == null ? Optional.empty() : Optional.of(entity.getUuid()));
	}
	
	public Vec3d getParentOffset(LivingEntity parent, float yaw, float pitch)
	{
		return IParentedEntity.getRotationVector(0F, yaw).normalize().multiply(0.4D);
	}
	
	public void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput)
	{
		this.prevFrameYaw = this.frameYaw;
		this.frameYaw = controllingPlayer.getBodyYaw();
	}
	
	public void tickParented(@NotNull LivingEntity parent)
	{
		if(parent == null) return;
		
		// Unbind from user if user is riding or holding two items
		if(!canUseWalker(parent, this))
			clearParent();
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}
}
