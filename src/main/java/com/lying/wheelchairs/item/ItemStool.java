package com.lying.wheelchairs.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.entity.EntityStool;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItems;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.DyeableItem;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ItemStool extends Item implements IBonusBlockItem, DyeableItem
{
	public ItemStool(Settings settings)
	{
		super(settings);
	}
	
	public static ItemStack withWheels(Item chair, Item wheels)
	{
		ItemStack defaultStack = chair.getDefaultStack();
		setWheels(defaultStack, wheels.getDefaultStack());
		return defaultStack;
	}
	
	public boolean isEnchantable(ItemStack stack) { return false; }
	
	public int getEnchantability() { return 0; }
	
	public ActionResult useOnBlock(ItemUsageContext context) {
		Direction direction = context.getSide();
		if(direction == Direction.DOWN)
			return ActionResult.FAIL;
		
		World world = context.getWorld();
		ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
		BlockPos blockPos = itemPlacementContext.getBlockPos();
		ItemStack itemStack = context.getStack();
		Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
		Box box = WHCEntityTypes.STOOL.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
		if(!world.isSpaceEmpty(null, box) || !world.getOtherEntities(null, box).isEmpty())
			return ActionResult.FAIL;
		
		if(world instanceof ServerWorld)
		{
			ServerWorld serverWorld = (ServerWorld)world;
			Consumer<EntityStool> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
			EntityStool wheelchair = WHCEntityTypes.STOOL.create(serverWorld, itemStack.getNbt(), consumer, blockPos, SpawnReason.SPAWN_EGG, true, true);
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
		
		tooltip.add(Text.translatable("gui.wheelchairs.stool.wheels", getWheels(stack).getName()));
	}
	
	public static void setWheels(ItemStack stack, ItemStack left)
	{
		NbtCompound data = stack.getOrCreateNbt();
		
		NbtCompound wheels = new NbtCompound();
		wheels.put("Wheels", left.writeNbt(new NbtCompound()));
		
		stack.setNbt(data);
	}
	
	public static ItemStack getWheels(ItemStack stack)
	{
		if(stack.getItem() instanceof ItemStool && stack.hasNbt())
		{
			NbtCompound data = stack.getNbt();
			if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
				return ItemStack.fromNbt(data.getCompound("Wheels"));
		}
		return new ItemStack(WHCItems.WHEEL_OAK);
	}
}
