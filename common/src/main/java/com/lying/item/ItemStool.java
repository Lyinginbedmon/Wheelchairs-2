package com.lying.item;

import java.util.function.Consumer;

import com.lying.entity.EntityStool;
import com.lying.init.WHCEntityTypes;

import net.minecraft.entity.SpawnReason;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ItemStool extends EntityPlacerItem<EntityStool> implements IBonusBlockItem, DyeableItem
{
	public static final int DEFAULT_COLOR = 0x1D1D21;
	
	public ItemStool(Settings settings)
	{
		super(WHCEntityTypes.STOOL, settings);
	}
	
	public boolean isEnchantable(ItemStack stack) { return false; }
	
	public int getEnchantability() { return 0; }
	
	public int getColor(ItemStack stack)
	{
		return hasColor(stack) ? stack.getOrCreateSubNbt(DISPLAY_KEY).getInt(COLOR_KEY) :  DEFAULT_COLOR;
	}
	
	protected EntityStool makeEntity(ServerWorld serverWorld, ItemStack stack, Consumer<EntityStool> consumer, BlockPos pos)
	{
		EntityStool stool = entityType.get().create(serverWorld, stack.getNbt(), consumer, pos, SpawnReason.SPAWN_EGG, true, true);
		if(stool != null)
			stool.copyFromItem(stack);
		return stool;
	}
}
