package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.WHCItemTags;
import com.lying.init.WHCItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class WHCItemTagsProvider extends ItemTagProvider
{
	public WHCItemTagsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}

	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(WHCItemTags.WHEEL).add(
				WHCItems.WHEEL_ACACIA, 
				WHCItems.WHEEL_BIRCH, 
				WHCItems.WHEEL_CRIMSON, 
				WHCItems.WHEEL_DARK_OAK, 
				WHCItems.WHEEL_JUNGLE, 
				WHCItems.WHEEL_MANGROVE, 
				WHCItems.WHEEL_OAK, 
				WHCItems.WHEEL_SPRUCE, 
				WHCItems.WHEEL_WARPED);
	}
}
