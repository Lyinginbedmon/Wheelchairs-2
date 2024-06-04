package com.lying.wheelchairs.item;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;

public class ItemCaneHandle extends Item
{
	public ItemCaneHandle(Settings settings)
	{
		super(settings);
	}
	
	public static class Dyeable extends ItemCaneHandle implements DyeableItem
	{
		public Dyeable(Settings settings)
		{
			super(settings);
		}
	}
}
