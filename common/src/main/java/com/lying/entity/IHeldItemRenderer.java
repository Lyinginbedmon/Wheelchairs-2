package com.lying.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public interface IHeldItemRenderer
{
	public ItemStack getHeldItem(Arm hand);
	
	public void setHeldItem(ItemStack stack, Arm hand);
}
