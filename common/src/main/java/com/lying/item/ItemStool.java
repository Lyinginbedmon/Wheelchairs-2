package com.lying.item;

import org.jetbrains.annotations.Nullable;

import com.lying.entity.EntityStool;
import com.lying.init.WHCEntityTypes;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ItemStool extends EntityPlacerItem<EntityStool> implements IBonusBlockItem
{
	public static final int DEFAULT_COLOR = 0x1D1D21;
	
	public ItemStool(Settings settings)
	{
		super(WHCEntityTypes.STOOL, settings.component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DEFAULT_COLOR, true)));
	}
	
	public boolean isEnchantable(ItemStack stack) { return false; }
	
	public int getEnchantability() { return 0; }
	
	public int getColor(ItemStack stack)
	{
		return DyedColorComponent.getColor(stack, DEFAULT_COLOR);
	}
	
	protected EntityStool makeEntity(ServerWorld serverWorld, ItemStack stack, @Nullable PlayerEntity player, BlockPos pos)
	{
		EntityStool stool = WHCEntityTypes.STOOL.get().spawnFromItemStack(serverWorld, stack, player, pos, SpawnReason.SPAWN_ITEM_USE, true, true);
		if(stool != null)
			stool.copyFromItem(stack);
		return stool;
	}
}
