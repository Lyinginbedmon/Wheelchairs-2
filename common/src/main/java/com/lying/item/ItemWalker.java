package com.lying.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.entity.EntityWalker;
import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
		super(settings
				.component(WHCDataComponentTypes.LEFT_WHEEL.get(), new ItemStack(WHCItems.WHEEL_OAK))
				.component(WHCDataComponentTypes.RIGHT_WHEEL.get(), new ItemStack(WHCItems.WHEEL_OAK))
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
			EntityWalker walker = WHCEntityTypes.WALKER.get().spawnFromItemStack(serverWorld, itemStack, context.getPlayer(), blockPos, SpawnReason.SPAWN_ITEM_USE, true, true);
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
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
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
		stack.set(WHCDataComponentTypes.LEFT_WHEEL.get(), left.copy());
		stack.set(WHCDataComponentTypes.RIGHT_WHEEL.get(), right.copy());
	}
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		ComponentType<ItemStack> entry = arm == Arm.LEFT ? WHCDataComponentTypes.LEFT_WHEEL.get() : WHCDataComponentTypes.RIGHT_WHEEL.get();
		if(stack.contains(entry))
			return stack.get(entry);
		return new ItemStack(WHCItems.WHEEL_OAK);
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