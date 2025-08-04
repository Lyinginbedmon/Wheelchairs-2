package com.lying.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import com.lying.component.type.WheelComponent;
import com.lying.init.WHCItems;
import com.lying.item.WalkerItem;
import com.lying.utility.WHCUtils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WalkerEntity extends LivingEntity implements IParentedEntity
{
	private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(WalkerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> LEFT_WHEEL = DataTracker.registerData(WalkerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<ItemStack> RIGHT_WHEEL = DataTracker.registerData(WalkerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Optional<UUID>> USER_ID = DataTracker.registerData(WalkerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	private static final TrackedData<Boolean> HAS_INV = DataTracker.registerData(WalkerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	private LivingEntity user = null;
	
	// FIXME Ensure walker wheels actually spin
	private Vector2d prevCaster, caster;
	public float spinLeft = 0F, spinRight = 0F;
	
	protected SimpleInventory items;
	
	public WalkerEntity(EntityType<? extends WalkerEntity> entityType, World world)
	{
		super(entityType, world);
		double randX = (getRandom().nextDouble() - 0.5D) * 2D;
		double randY = (getRandom().nextDouble() - 0.5D) * 2D;
		prevCaster = caster = new Vector2d(randX, randY);
	}
	
	public void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		
		builder.add(ITEM, WHCItems.WALKER_OAK.get().getDefaultStack());
		builder.add(LEFT_WHEEL, WheelComponent.DEFAULT_WHEEL.get());
		builder.add(RIGHT_WHEEL, WheelComponent.DEFAULT_WHEEL.get());
		builder.add(USER_ID, Optional.empty());
		builder.add(HAS_INV, false);
	}
	
	public static DefaultAttributeContainer.Builder createWalkerAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 1F).add(EntityAttributes.MOVEMENT_SPEED, 0.1F).add(EntityAttributes.STEP_HEIGHT, 0.5F);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		
		if(data.contains("Item", NbtElement.COMPOUND_TYPE))
			getDataTracker().set(ITEM, ItemStack.fromNbt(getRegistryManager(), data.getCompound("Item")).get());
		else if(data.contains("Chair", NbtElement.COMPOUND_TYPE))
			getDataTracker().set(ITEM, ItemStack.fromNbt(getRegistryManager(), data.getCompound("Chair")).get());
		
		if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound wheels = data.getCompound("Wheels");
			getDataTracker().set(LEFT_WHEEL, ItemStack.fromNbt(getRegistryManager(), wheels.getCompound("Left")).get());
			getDataTracker().set(RIGHT_WHEEL, ItemStack.fromNbt(getRegistryManager(), wheels.getCompound("Right")).get());
		}
		
		setHasInventory(data.getBoolean("Chested"));
		if(data.contains("Items", NbtElement.LIST_TYPE))
		{
			NbtList items = data.getList("Items", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<items.size(); ++i)
			{
				NbtCompound nbt = items.getCompound(i);
				int j = nbt.getByte("Slot") & 0xFF;
				if (j < this.items.size())
					this.items.setStack(j, ItemStack.fromNbt(getRegistryManager(), nbt).get());
			}
		}
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.put("Item", getDataTracker().get(ITEM).toNbt(getRegistryManager()));
		NbtCompound wheels = new NbtCompound();
			wheels.put("Left", getLeftWheel().toNbt(getRegistryManager()));
			wheels.put("Right", getRightWheel().toNbt(getRegistryManager()));
		data.put("Wheels", wheels);
		
		data.putBoolean("Chested", hasInventory());
		if(hasInventory())
		{
			NbtList items = new NbtList();
			for(int i=0; i<this.items.size(); i++)
			{
				ItemStack stack = this.items.getStack(i);
				if(stack.isEmpty())
					continue;
				NbtCompound nbt = (NbtCompound)stack.toNbt(getRegistryManager());
				nbt.putByte("Slot", (byte)i);
				items.add(nbt);
			}
			data.put("Items", items);
		}
	}
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		/* If this walker either has no parent OR the interacting player is the parent */
		boolean shouldRespond = !hasParent() || isParent(player);
		boolean isServer = !getWorld().isClient();
		ItemStack heldStack = player.getStackInHand(hand);
		if(player.shouldCancelInteraction())
		{
			boolean hasInv = hasInventory();
			// Chest upgrade application
			if(!hasInv && (heldStack.isOf(Items.CHEST) || heldStack.isOf(Items.TRAPPED_CHEST)))
			{
				addInventory();
				if(!player.getAbilities().creativeMode)
					heldStack.decrement(1);
			}
			// Chest upgrade removal
			else if(hasInv && heldStack.isIn(ItemTags.AXES))
			{
				if(isServer)
					dropItem((ServerWorld)getWorld(), Items.CHEST);
				setHasInventory(false);
				
				if(!player.isCreative())
					heldStack.damage(1, player);
				playSound(SoundEvents.ITEM_AXE_STRIP, getSoundVolume(), getSoundPitch());
			}
			// Item conversion
			else if(shouldRespond)
				convertToItem(null);
			return ActionResult.CONSUME;
		}
		else if(!getWorld().isClient())
			return shouldRespond && IParentedEntity.bindToPlayer(player, this) ? ActionResult.CONSUME : ActionResult.PASS;
		
		return ActionResult.SUCCESS;
	}
	
	public static ItemStack chairToItem(WalkerEntity chair)
	{
		ItemStack stack = chair.getFrame();
		WalkerItem.setWheels(stack, chair.getLeftWheel(), chair.getRightWheel());
		WalkerItem.setHasChest(stack, chair.hasInventory());
		return stack;
	}
	
	public void copyFromItem(ItemStack stack)
	{
		getDataTracker().set(ITEM, stack.copy());
		getDataTracker().set(LEFT_WHEEL, WalkerItem.getWheel(stack, Arm.LEFT));
		getDataTracker().set(RIGHT_WHEEL, WalkerItem.getWheel(stack, Arm.RIGHT));
		if(WalkerItem.hasChest(stack))
		{
			setHasInventory(true);
			List<ItemStack> storedItems = stack.get(DataComponentTypes.CONTAINER).stream().toList();
			for(int i=0; i<storedItems.size(); i++)
				this.items.setStack(i, storedItems.get(i));
		}
	}
	
	@Nullable
	public LivingEntity getParent()
	{
		if(!hasParent())
			return null;
		
		return user == null ? (user = IParentedEntity.getParentOf(this)) : user;
	}
	
	/** Converts this wheelchair into an ItemEntity or (if a player is supplied) an ItemStack in a player's inventory */
	public void convertToItem(@Nullable PlayerEntity player)
	{
		if(!getWorld().isClient())
		{
			ItemStack stack = chairToItem(this);
			ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), stack);
			dropInventory((ServerWorld)getWorld());
			
			if(player == null || !player.getInventory().insertStack(stack))
				getWorld().spawnEntity(item);
			discard();
		}
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
	
	public boolean isInvulnerableTo(ServerWorld world, DamageSource damageSource)
	{
		DamageSources sources = world.getDamageSources();
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
		spoof.set(DataComponentTypes.ENCHANTMENTS, chair.get(DataComponentTypes.ENCHANTMENTS));
		return spoof;
	}
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
	
	public ItemStack getFrame()
	{
		ItemStack stack = getDataTracker().get(ITEM);
		return stack.getItem() instanceof WalkerItem ? stack : new ItemStack(WHCItems.WHEELCHAIR_OAK);
	}
	
	public ItemStack getWheel(Arm arm) { return arm == Arm.LEFT ? getLeftWheel() : getRightWheel(); }
	protected ItemStack getWheel(ItemStack actualWheel)
	{
		ItemStack wheel = actualWheel.getItem().getDefaultStack().copy();
		wheel.set(DataComponentTypes.ENCHANTMENTS, getFrame().get(DataComponentTypes.ENCHANTMENTS));
		return wheel;
	}
	public ItemStack getLeftWheel() { return getWheel(getDataTracker().get(LEFT_WHEEL)); }
	public ItemStack getRightWheel() { return getWheel(getDataTracker().get(RIGHT_WHEEL)); }
	
	public boolean hasParent() { return getDataTracker().get(USER_ID).isPresent(); }
	
	public boolean isParent(Entity entity) { return hasParent() && entity.getUuid().equals(getDataTracker().get(USER_ID).get()); }
	
	public void parentTo(@Nullable LivingEntity entity)
	{
		if(getWorld().isClient()) return;
		getDataTracker().set(USER_ID, entity == null ? Optional.empty() : Optional.of(entity.getUuid()));
	}
	
	public Vec3d getParentOffset(LivingEntity parent, float yaw, float pitch)
	{
		return IParentedEntity.rotateOffset(new Vec3d(0, 0, 0.5D), yaw);
	}
	
	public void tick()
	{
		super.tick();
		if(!getWorld().isClient())
			serverTick();
	}
	
	private void serverTick()
	{
		if(hasParent() && getParent() == null)
			clearParent();
	}
	
	public void tickParented(@NotNull LivingEntity parent, float yaw, float pitch)
	{
		this.prevYaw = yaw;
		this.bodyYaw = yaw;
		this.prevPitch = 0F;
		this.setPitch(0F);
		
		// Unbind from user if user is riding or holding two items
		if(!canParentToChild(parent, this))
			clearParent();
	}
	
	public void move(MovementType movementType, Vec3d movement)
	{
		super.move(movementType, movement);
		
		Vector2d delta = new Vector2d(movement.x, movement.z);
		if(delta.length() == 0D || !getWorld().isClient()) return;
		
		float spin = WHCUtils.calculateSpin((float)delta.length(), 5F / 16F);
		this.spinLeft = WHCUtils.wrapDegrees(this.spinLeft + spin);
		this.spinRight = WHCUtils.wrapDegrees(this.spinRight + spin);
		
		caster.get(prevCaster);
		caster.add(delta.mul(0.5D)).normalize();
	}
	
	public float casterWheelYaw(float tickDelta)
	{
		// Values cloned because Vector2d performs all operations on the value itself instead of returning new ones
		Vector2d origin = prevCaster.get(new Vector2d());
		Vector2d current = caster.get(new Vector2d());
		origin.add(current.sub(origin).mul(tickDelta));
		return (float)Math.toDegrees(Math.atan2(origin.y, origin.x));
	}
	
	public boolean hasInventory() { return getDataTracker().get(HAS_INV).booleanValue(); }
	
	public void addInventory()
	{
		if(hasInventory())
			return;
		
		setHasInventory(true);
		playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), getSoundVolume(), getSoundPitch());
	}
	
	public void setHasInventory(boolean bool)
	{
		getDataTracker().set(HAS_INV, bool);
		onChestedStatusChanged();
	}
	
	public Inventory getInventory() { return this.items; }
	
	protected void onChestedStatusChanged()
	{
		if(!hasInventory() && !getWorld().isClient())
			dropInventory((ServerWorld)getWorld());
		
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
}
