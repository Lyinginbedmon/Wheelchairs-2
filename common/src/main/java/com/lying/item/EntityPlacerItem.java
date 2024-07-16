package com.lying.item;

import java.util.function.Consumer;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

public abstract class EntityPlacerItem<T extends Entity> extends Item
{
	protected final RegistrySupplier<EntityType<T>> entityType;
	
	protected EntityPlacerItem(RegistrySupplier<EntityType<T>> typeIn, Settings settings)
	{
		super(settings);
		entityType = typeIn;
	}
	
	public ActionResult useOnBlock(ItemUsageContext context) {
		Direction direction = context.getSide();
		if(direction == Direction.DOWN)
			return ActionResult.FAIL;
		
		World world = context.getWorld();
		ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
		BlockPos blockPos = itemPlacementContext.getBlockPos();
		ItemStack itemStack = context.getStack();
		Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
		Box box = entityType.get().getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
		if(!world.isSpaceEmpty(null, box) || !world.getOtherEntities(null, box).isEmpty())
			return ActionResult.FAIL;
		
		if(world instanceof ServerWorld)
		{
			ServerWorld serverWorld = (ServerWorld)world;
			Consumer<T> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
			T entity = makeEntity(serverWorld, itemStack, consumer, blockPos);
			if (entity == null)
				return ActionResult.FAIL;
			
			float f = (float)MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
			entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), f, 0.0f);
			serverWorld.spawnEntityAndPassengers(entity);
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75f, 0.8f);
			entity.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
		}
		
		itemStack.decrement(1);
		return ActionResult.success(world.isClient);
	}
	
	protected abstract T makeEntity(ServerWorld serverWorld, ItemStack stack, Consumer<T> consumer, BlockPos pos);
}
