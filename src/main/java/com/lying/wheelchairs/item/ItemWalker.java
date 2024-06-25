package com.lying.wheelchairs.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItems;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ItemWalker extends Item implements IBonusBlockItem
{
	public ItemWalker(Settings settings)
	{
		super(settings);
	}
	
	public static ItemStack withWheels(Item chair, Item wheels)
	{
		ItemStack defaultStack = chair.getDefaultStack();
		setWheels(defaultStack, wheels.getDefaultStack(), wheels.getDefaultStack());
		return defaultStack;
	}
	
	public boolean isEnchantable(ItemStack stack) { return getMaxCount() == 1; }
	
	public int getEnchantability() { return 5; }
	
	public ActionResult useOnBlock(ItemUsageContext context) {
		Direction direction = context.getSide();
		if(direction == Direction.DOWN)
			return ActionResult.FAIL;
		
		World world = context.getWorld();
		ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
		BlockPos blockPos = itemPlacementContext.getBlockPos();
		ItemStack itemStack = context.getStack();
		Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
		Box box = WHCEntityTypes.WALKER.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
		if(!world.isSpaceEmpty(null, box) || !world.getOtherEntities(null, box).isEmpty())
			return ActionResult.FAIL;
		
		if(world instanceof ServerWorld)
		{
			ServerWorld serverWorld = (ServerWorld)world;
			Consumer<EntityWalker> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
			EntityWalker wheelchair = WHCEntityTypes.WALKER.create(serverWorld, itemStack.getNbt(), consumer, blockPos, SpawnReason.SPAWN_EGG, true, true);
			if (wheelchair == null)
				return ActionResult.FAIL;
			
			wheelchair.copyFromItem(itemStack);
			
			float f = (float)MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
			wheelchair.refreshPositionAndAngles(wheelchair.getX(), wheelchair.getY(), wheelchair.getZ(), f, 0.0f);
			serverWorld.spawnEntityAndPassengers(wheelchair);
			world.playSound(null, wheelchair.getX(), wheelchair.getY(), wheelchair.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75f, 0.8f);
			wheelchair.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
		}
		
		itemStack.decrement(1);
		return ActionResult.success(world.isClient);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!stack.hasNbt())
			return;
		
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_left", getWheel(stack, Arm.LEFT).getName()));
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_right", getWheel(stack, Arm.RIGHT).getName()));
	}
	
	public static Iterable<ItemStack> getWheels(ItemStack stack)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, new ItemStack(WHCItems.WHEEL_OAK));
		wheels.set(0, getWheel(stack, Arm.LEFT));
		wheels.set(1, getWheel(stack, Arm.RIGHT));
		return wheels;
	}
	
	public static void setWheels(ItemStack stack, ItemStack left, ItemStack right)
	{
		NbtCompound data = stack.getOrCreateNbt();
		
		NbtCompound wheels = new NbtCompound();
		wheels.put("Left", left.writeNbt(new NbtCompound()));
		wheels.put("Right", right.writeNbt(new NbtCompound()));
		data.put("Wheels", wheels);
		
		stack.setNbt(data);
	}
	
	public static void setHasChest(ItemStack stack)
	{
		NbtCompound data = stack.getOrCreateNbt();
		data.putBoolean("HasChest", true);
		stack.setNbt(data);
	}
	
	public static boolean hasChest(ItemStack stack)
	{
		return stack.getOrCreateNbt().getBoolean("HasChest");
	}
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		String entry = arm == Arm.LEFT ? "Left" : "Right";
		ItemStack wheel = new ItemStack(WHCItems.WHEEL_OAK);
		if(stack.getItem() instanceof ItemWalker && stack.hasNbt())
		{
			NbtCompound data = stack.getNbt();
			if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
			{
				data = data.getCompound("Wheels");
				if(data.contains(entry, NbtElement.COMPOUND_TYPE))
					wheel = ItemStack.fromNbt(data.getCompound(entry));
			}
		}
		return wheel;
	}
}
