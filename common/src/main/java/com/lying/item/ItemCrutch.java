package com.lying.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;

public class ItemCrutch extends Item
{
	public ItemCrutch(Settings settings)
	{
		super(settings.component(DataComponentTypes.DYED_COLOR, null));
	}
}
