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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
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
	private static final TrackedData<Boolean> HAS_INV = DataTracker.registerData(EntityWalker.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	/*
	 * TODO Add walker crafting recipe
	 */
	
	private LivingEntity user = null;
	public float prevFrameYaw, frameYaw = 0F;
	public float prevCasterYaw, casterYaw = 0F;
	public float spinLeft, spinRight;
	
	protected SimpleInventory items;
	
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
		this.getDataTracker().startTracking(HAS_INV, false);
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
		
		if(data.contains("Items", NbtElement.LIST_TYPE))
		{
			setHasInventory();
			
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
		NbtCompound wheels = new NbtCompound();
			wheels.put("Left", getLeftWheel().writeNbt(new NbtCompound()));
			wheels.put("Right", getRightWheel().writeNbt(new NbtCompound()));
		data.put("Wheels", wheels);
		
		if(hasParent())
			data.putUuid("User", getDataTracker().get(USER_ID).get());
		
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
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getStackInHand(hand);
		if(player.shouldCancelInteraction())
		{
			if(!hasInventory() && (heldStack.isOf(Items.CHEST) || heldStack.isOf(Items.TRAPPED_CHEST)))
			{
				addInventory();
				if(!player.getAbilities().creativeMode)
					heldStack.decrement(1);
			}
			else if(hasInventory() && heldStack.isIn(ItemTags.AXES))
			{
				dropItem(Items.CHEST);
				dropInventory();
				getDataTracker().set(HAS_INV, false);
				if(!player.isCreative())
					heldStack.damage(1, player, playerx -> playerx.sendToolBreakStatus(hand));
				playSound(SoundEvents.ITEM_AXE_STRIP, getSoundVolume(), getSoundPitch());
			}
			else
				convertToItem(null);
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
		ItemWalker.setHasChest(stack);
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(CHAIR, stack.copy());
		getDataTracker().set(LEFT_WHEEL, ItemWalker.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, ItemWalker.getWheel(stack, Arm.RIGHT));
		if(ItemWalker.hasChest(stack))
			setHasInventory();
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
		if(entity != null)
			System.out.println("Parenting link established between "+getName().getString()+" and "+entity.getName().getString());
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
		{
			clearParent();
			System.out.println("Parenting link severed between "+getName().getString()+" and "+parent.getName().getString());
		}
	}
	
	protected float getSaddledSpeed(PlayerEntity controllingPlayer)
	{
		return (float)controllingPlayer.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}
	
	public boolean hasInventory() { return getDataTracker().get(HAS_INV).booleanValue(); }
	
	public void addInventory()
	{
		if(hasInventory())
			return;
		
		setHasInventory();
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON, getSoundVolume(), getSoundPitch());
	}
	
	public void setHasInventory()
	{
		getDataTracker().set(HAS_INV, true);
		onChestedStatusChanged();
	}
	
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
}
