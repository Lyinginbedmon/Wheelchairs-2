package com.lying.neoforge;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class VestChangeEvent extends EntityEvent
{
	private final ItemStack vest;
	
	public VestChangeEvent(Entity ent, ItemStack vestIn)
	{
		super(ent);
		vest = vestIn.copy();
	}
	
	public boolean isEmpty() { return vest.isEmpty(); }
	
	public ItemStack vest() { return vest; }
}
