package com.lying.wheelchairs.item;

import java.util.function.Consumer;

import com.lying.wheelchairs.entity.EntityStool;
import com.lying.wheelchairs.init.WHCEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
}
