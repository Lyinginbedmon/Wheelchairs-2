package com.lying.utility;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface XPlatHandler
{
	public boolean hasVest(LivingEntity entity);
	
	public ItemStack getVest(LivingEntity entity);
	
	public void setVest(LivingEntity entity, ItemStack stack);
}
