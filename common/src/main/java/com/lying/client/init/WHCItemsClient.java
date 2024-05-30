package com.lying.client.init;

import com.lying.init.WHCItems;

import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.minecraft.item.DyeableItem;

public class WHCItemsClient
{
	public static void registerItemColors()
	{
		ColorHandlerRegistry.registerItemColors((stack, index) -> { return index == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
				WHCItems.WHEELCHAIR_ACACIA,
				WHCItems.WHEELCHAIR_BIRCH,
				WHCItems.WHEELCHAIR_DARK_OAK,
				WHCItems.WHEELCHAIR_JUNGLE,
				WHCItems.WHEELCHAIR_OAK,
				WHCItems.WHEELCHAIR_SPRUCE,
				WHCItems.WHEELCHAIR_CHERRY,
				WHCItems.WHEELCHAIR_MANGROVE,
				WHCItems.WHEELCHAIR_BAMBOO);
	}
}
