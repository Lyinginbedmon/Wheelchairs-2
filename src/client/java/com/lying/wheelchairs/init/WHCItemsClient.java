package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;

public class WHCItemsClient
{
	public static final Map<Item, ModelIdentifier> CRUTCH_MAP = new HashMap<>();
	
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
				WHCItems.WHEELCHAIR_BAMBOO,
				
				WHCItems.CRUTCH_ACACIA,
				WHCItems.CRUTCH_BIRCH,
				WHCItems.CRUTCH_DARK_OAK,
				WHCItems.CRUTCH_JUNGLE,
				WHCItems.CRUTCH_OAK,
				WHCItems.CRUTCH_SPRUCE,
				WHCItems.CRUTCH_CHERRY,
				WHCItems.CRUTCH_MANGROVE,
				WHCItems.CRUTCH_BAMBOO);
	}
	
	private static void addCrutch(Item item)
	{
		CRUTCH_MAP.put(item, new ModelIdentifier(Reference.ModInfo.MOD_ID, item.getTranslationKey()+"_inventory", "inventory"));
	}
	
	static
	{
		addCrutch(WHCItems.CRUTCH_ACACIA);
		addCrutch(WHCItems.CRUTCH_BAMBOO);
		addCrutch(WHCItems.CRUTCH_BIRCH);
		addCrutch(WHCItems.CRUTCH_CHERRY);
		addCrutch(WHCItems.CRUTCH_CRIMSON);
		addCrutch(WHCItems.CRUTCH_DARK_OAK);
		addCrutch(WHCItems.CRUTCH_JUNGLE);
		addCrutch(WHCItems.CRUTCH_MANGROVE);
		addCrutch(WHCItems.CRUTCH_OAK);
		addCrutch(WHCItems.CRUTCH_SPRUCE);
		addCrutch(WHCItems.CRUTCH_WARPED);
	}
}
