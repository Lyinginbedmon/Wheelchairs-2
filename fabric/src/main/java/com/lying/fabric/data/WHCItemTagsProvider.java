package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.WHCTags;
import com.lying.init.WHCItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;

public class WHCItemTagsProvider extends ItemTagProvider
{
	public WHCItemTagsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(ItemTags.SWORDS);
		
		getOrCreateTagBuilder(WHCTags.WHEEL).add(
				WHCItems.WHEEL_ACACIA.get(), 
				WHCItems.WHEEL_BIRCH.get(), 
				WHCItems.WHEEL_CRIMSON.get(), 
				WHCItems.WHEEL_DARK_OAK.get(), 
				WHCItems.WHEEL_JUNGLE.get(), 
				WHCItems.WHEEL_MANGROVE.get(), 
				WHCItems.WHEEL_OAK.get(), 
				WHCItems.WHEEL_SPRUCE.get(), 
				WHCItems.WHEEL_WARPED.get(),
				WHCItems.WHEEL_CHERRY.get(),
				WHCItems.WHEEL_BAMBOO.get(),
				WHCItems.WHEEL_COPPER.get(),
				WHCItems.WHEEL_IRON.get(),
				WHCItems.WHEEL_GOLD.get(),
				WHCItems.WHEEL_NETHERITE.get());
		getOrCreateTagBuilder(WHCTags.WHEELCHAIR).add(
				WHCItems.WHEELCHAIR_ACACIA.get(), 
				WHCItems.WHEELCHAIR_BIRCH.get(), 
				WHCItems.WHEELCHAIR_CRIMSON.get(), 
				WHCItems.WHEELCHAIR_DARK_OAK.get(), 
				WHCItems.WHEELCHAIR_JUNGLE.get(), 
				WHCItems.WHEELCHAIR_MANGROVE.get(), 
				WHCItems.WHEELCHAIR_OAK.get(), 
				WHCItems.WHEELCHAIR_SPRUCE.get(), 
				WHCItems.WHEELCHAIR_WARPED.get(),
				WHCItems.WHEELCHAIR_CHERRY.get(),
				WHCItems.WHEELCHAIR_BAMBOO.get());
		getOrCreateTagBuilder(WHCTags.CRUTCH).add(
				WHCItems.CRUTCH_ACACIA.get(), 
				WHCItems.CRUTCH_BIRCH.get(), 
				WHCItems.CRUTCH_CRIMSON.get(), 
				WHCItems.CRUTCH_DARK_OAK.get(), 
				WHCItems.CRUTCH_JUNGLE.get(), 
				WHCItems.CRUTCH_MANGROVE.get(), 
				WHCItems.CRUTCH_OAK.get(), 
				WHCItems.CRUTCH_SPRUCE.get(), 
				WHCItems.CRUTCH_WARPED.get(),
				WHCItems.CRUTCH_CHERRY.get(),
				WHCItems.CRUTCH_BAMBOO.get());
		getOrCreateTagBuilder(WHCTags.CANE).add(
				WHCItems.CANE_ACACIA.get(), 
				WHCItems.CANE_BIRCH.get(), 
				WHCItems.CANE_CRIMSON.get(), 
				WHCItems.CANE_DARK_OAK.get(), 
				WHCItems.CANE_JUNGLE.get(), 
				WHCItems.CANE_MANGROVE.get(), 
				WHCItems.CANE_OAK.get(), 
				WHCItems.CANE_SPRUCE.get(), 
				WHCItems.CANE_WARPED.get(),
				WHCItems.CANE_CHERRY.get(),
				WHCItems.CANE_BAMBOO.get());
		getOrCreateTagBuilder(WHCTags.WALKER).add(
				WHCItems.WALKER_ACACIA.get(),
				WHCItems.WALKER_BAMBOO.get(),
				WHCItems.WALKER_BIRCH.get(),
				WHCItems.WALKER_CHERRY.get(),
				WHCItems.WALKER_CRIMSON.get(),
				WHCItems.WALKER_DARK_OAK.get(),
				WHCItems.WALKER_JUNGLE.get(),
				WHCItems.WALKER_MANGROVE.get(),
				WHCItems.WALKER_OAK.get(),
				WHCItems.WALKER_SPRUCE.get(),
				WHCItems.WALKER_WARPED.get());
		getOrCreateTagBuilder(WHCTags.PRESERVED).addTag(WHCTags.WHEELCHAIR).addTag(WHCTags.CRUTCH).addTag(WHCTags.CANE).addTag(WHCTags.WALKER).add(WHCItems.TABLET.get());
		
		getOrCreateTagBuilder(WHCTags.FILTER_SWORD_CANE).addTag(ItemTags.SWORDS);
		
		getOrCreateTagBuilder(ItemTags.DYEABLE).add(WHCItems.VEST.get(), WHCItems.STOOL.get());
	}
}
