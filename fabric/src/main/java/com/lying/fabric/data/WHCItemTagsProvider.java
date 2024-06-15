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
				WHCItems.WHEEL_ACACIA.get(), 
				WHCItems.WHEEL_BIRCH.get(), 
				WHCItems.WHEEL_CRIMSON.get(), 
				WHCItems.WHEEL_DARK_OAK.get(), 
				WHCItems.WHEEL_JUNGLE.get(), 
				WHCItems.WHEEL_MANGROVE.get(), 
				WHCItems.WHEEL_OAK.get(), 
				WHCItems.WHEEL_SPRUCE.get(), 
				WHCItems.WHEEL_WARPED.get(),
				WHCItems.WHEEL_BAMBOO.get(),
				WHCItems.WHEEL_CHERRY.get());
	}
}
