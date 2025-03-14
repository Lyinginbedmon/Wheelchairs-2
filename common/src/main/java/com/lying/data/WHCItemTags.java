package com.lying.data;

import static com.lying.reference.Reference.ModInfo.prefix;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class WHCItemTags
{
	public static final TagKey<Item> WHEEL = TagKey.of(RegistryKeys.ITEM, prefix("wheels"));
	public static final TagKey<Item> WHEELCHAIR = TagKey.of(RegistryKeys.ITEM, prefix("wheelchair"));
	public static final TagKey<Item> CRUTCH = TagKey.of(RegistryKeys.ITEM, prefix("crutch"));
	public static final TagKey<Item> CANE = TagKey.of(RegistryKeys.ITEM, prefix("cane"));
	public static final TagKey<Item> WALKER = TagKey.of(RegistryKeys.ITEM, prefix("walker"));
	public static final TagKey<Item> PRESERVED = TagKey.of(RegistryKeys.ITEM, prefix("preserved"));
	
	public static final TagKey<Item> FILTER_SWORD_CANE = TagKey.of(RegistryKeys.ITEM, prefix("cane_filter"));
}
