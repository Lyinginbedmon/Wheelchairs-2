package com.lying.utility;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface XPlatHandler
{
	public default boolean hasVest(LivingEntity entity) { return false; }
	
	public default ItemStack getVest(LivingEntity entity) { return ItemStack.EMPTY; }
	
	public default void setVest(LivingEntity entity, ItemStack stack) { }
}
