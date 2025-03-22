package com.lying.data;

import static com.lying.reference.Reference.ModInfo.prefix;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class WHCTags
{
	public static final TagKey<Item> WHEEL		= TagKey.of(RegistryKeys.ITEM, prefix("wheels"));
	public static final TagKey<Item> WHEELCHAIR	= TagKey.of(RegistryKeys.ITEM, prefix("wheelchairs"));
	public static final TagKey<Item> CRUTCH		= TagKey.of(RegistryKeys.ITEM, prefix("crutches"));
	public static final TagKey<Item> CANE		= TagKey.of(RegistryKeys.ITEM, prefix("canes"));
	public static final TagKey<Item> WALKER		= TagKey.of(RegistryKeys.ITEM, prefix("walkers"));
	public static final TagKey<Item> PRESERVED	= TagKey.of(RegistryKeys.ITEM, prefix("preserved"));
	
	public static final TagKey<Item> FILTER_SWORD_CANE = TagKey.of(RegistryKeys.ITEM, prefix("cane_filter"));
	
	public static final TagKey<Enchantment> CANE_INTERACT_SET	= TagKey.of(RegistryKeys.ENCHANTMENT, prefix("cane_interact_exclusive"));
}
