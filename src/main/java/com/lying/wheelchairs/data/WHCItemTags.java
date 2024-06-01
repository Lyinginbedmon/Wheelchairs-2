package com.lying.wheelchairs.data;

import java.util.concurrent.CompletableFuture;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class WHCItemTags extends ItemTagProvider
{
	public static final TagKey<Item> WHEEL = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheels"));
	public static final TagKey<Item> WHEELCHAIR = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheelchair"));
	public static final TagKey<Item> CRUTCH = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "crutch"));
	//public static final TagKey<Item> CANE = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "cane"));
	public static final TagKey<Item> PRESERVED = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "preserved"));
	
	public WHCItemTags(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(WHEEL).add(
				WHCItems.WHEEL_ACACIA, 
				WHCItems.WHEEL_BIRCH, 
				WHCItems.WHEEL_CRIMSON, 
				WHCItems.WHEEL_DARK_OAK, 
				WHCItems.WHEEL_JUNGLE, 
				WHCItems.WHEEL_MANGROVE, 
				WHCItems.WHEEL_OAK, 
				WHCItems.WHEEL_SPRUCE, 
				WHCItems.WHEEL_WARPED,
				WHCItems.WHEEL_CHERRY,
				WHCItems.WHEEL_BAMBOO);
		getOrCreateTagBuilder(WHEELCHAIR).add(
				WHCItems.WHEELCHAIR_ACACIA, 
				WHCItems.WHEELCHAIR_BIRCH, 
				WHCItems.WHEELCHAIR_CRIMSON, 
				WHCItems.WHEELCHAIR_DARK_OAK, 
				WHCItems.WHEELCHAIR_JUNGLE, 
				WHCItems.WHEELCHAIR_MANGROVE, 
				WHCItems.WHEELCHAIR_OAK, 
				WHCItems.WHEELCHAIR_SPRUCE, 
				WHCItems.WHEELCHAIR_WARPED,
				WHCItems.WHEELCHAIR_CHERRY,
				WHCItems.WHEELCHAIR_BAMBOO);
		getOrCreateTagBuilder(CRUTCH).add(
				WHCItems.CRUTCH_ACACIA, 
				WHCItems.CRUTCH_BIRCH, 
				WHCItems.CRUTCH_CRIMSON, 
				WHCItems.CRUTCH_DARK_OAK, 
				WHCItems.CRUTCH_JUNGLE, 
				WHCItems.CRUTCH_MANGROVE, 
				WHCItems.CRUTCH_OAK, 
				WHCItems.CRUTCH_SPRUCE, 
				WHCItems.CRUTCH_WARPED,
				WHCItems.CRUTCH_CHERRY,
				WHCItems.CRUTCH_BAMBOO);
		getOrCreateTagBuilder(PRESERVED).addTag(CRUTCH);
	}
}
