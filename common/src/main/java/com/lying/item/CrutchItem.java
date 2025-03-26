package com.lying.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;

public class CrutchItem extends Item
{
	public CrutchItem(Settings settings)
	{
		super(settings.component(DataComponentTypes.DYED_COLOR, null));
	}
}
