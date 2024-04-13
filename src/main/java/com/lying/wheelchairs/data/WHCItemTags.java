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
	public static final TagKey<Item> WHEEL = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheel"));
	
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
				WHCItems.WHEEL_WARPED);
	}
}
