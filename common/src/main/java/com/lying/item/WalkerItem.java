package com.lying.item;

import java.util.List;

import com.lying.component.type.WheelComponent;
import com.lying.entity.WalkerEntity;
import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCEntityTypes;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
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

public class WalkerItem extends Item implements IBonusBlockItem
{
	public WalkerItem(Settings settings)
	{
		super(settings
				.component(WHCDataComponentTypes.LEFT_WHEEL.get(), WheelComponent.empty(Arm.LEFT))
				.component(WHCDataComponentTypes.RIGHT_WHEEL.get(), WheelComponent.empty(Arm.RIGHT))
				.component(WHCDataComponentTypes.HAS_CHEST.get(), false)
				.component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT));
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
		Box box = WHCEntityTypes.WALKER.get().getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
		if(!world.isSpaceEmpty(null, box) || !world.getOtherEntities(null, box).isEmpty())
			return ActionResult.FAIL;
		
		if(world instanceof ServerWorld)
		{
			ServerWorld serverWorld = (ServerWorld)world;
			WalkerEntity walker = WHCEntityTypes.WALKER.get().spawnFromItemStack(serverWorld, itemStack, context.getPlayer(), blockPos, SpawnReason.SPAWN_ITEM_USE, true, true);
			if (walker == null)
				return ActionResult.FAIL;
			
			walker.copyFromItem(itemStack);
			
			float f = (float)MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
			walker.refreshPositionAndAngles(walker.getX(), walker.getY(), walker.getZ(), f, 0.0f);
			serverWorld.spawnEntityAndPassengers(walker);
			world.playSound(null, walker.getX(), walker.getY(), walker.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75f, 0.8f);
			walker.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
		}
		
		itemStack.decrement(1);
		return ActionResult.SUCCESS_SERVER;
	}
	
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type)
	{
		stack.get(WHCDataComponentTypes.LEFT_WHEEL.get()).appendTooltip(context, tooltip::add, type);
		stack.get(WHCDataComponentTypes.RIGHT_WHEEL.get()).appendTooltip(context, tooltip::add, type);
	}
	
	public static Iterable<ItemStack> getWheels(ItemStack stack)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, WheelComponent.DEFAULT_WHEEL.get());
		wheels.set(0, getWheel(stack, Arm.LEFT));
		wheels.set(1, getWheel(stack, Arm.RIGHT));
		return wheels;
	}
	
	public static void setWheels(ItemStack stack, ItemStack left, ItemStack right)
	{
		stack.set(WHCDataComponentTypes.LEFT_WHEEL.get(), stack.get(WHCDataComponentTypes.LEFT_WHEEL.get()).with(left));
		stack.set(WHCDataComponentTypes.RIGHT_WHEEL.get(), stack.get(WHCDataComponentTypes.RIGHT_WHEEL.get()).with(right));
	}
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		ComponentType<WheelComponent> entry = arm == Arm.LEFT ? WHCDataComponentTypes.LEFT_WHEEL.get() : WHCDataComponentTypes.RIGHT_WHEEL.get();
		return stack.contains(entry) ? stack.get(entry).item() : WheelComponent.DEFAULT_WHEEL.get();
	}
	
	public static void setHasChest(ItemStack stack, boolean contents)
	{
		stack.set(WHCDataComponentTypes.HAS_CHEST.get(), contents);
	}
	
	public static boolean hasChest(ItemStack stack)
	{
		return stack.contains(WHCDataComponentTypes.HAS_CHEST.get()) ? stack.get(WHCDataComponentTypes.HAS_CHEST.get()) : false;
	}
}