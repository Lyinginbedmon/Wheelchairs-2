package com.lying.wheelchairs.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.DyeableItem;

public class WHCItemsClient
{
	public static void registerItemColors()
	{
		ColorProviderRegistry.ITEM.register((stack, index) -> { return index == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
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
