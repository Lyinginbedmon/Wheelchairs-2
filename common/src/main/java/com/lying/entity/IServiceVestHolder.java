package com.lying.entity;

import net.minecraft.item.ItemStack;

public interface IServiceVestHolder
{
	public default boolean hasVest() { return !getVest().isEmpty(); }
	
	public ItemStack getVest();
	
	public void setVest(ItemStack stack);
}
