package com.lying.data;

import com.lying.reference.Reference;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class WHCItemTags
{
	public static final TagKey<Item> WHEEL = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheel"));
	public static final TagKey<Item> WHEELCHAIR = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheelchair"));
	public static final TagKey<Item> CRUTCH = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "crutch"));
	public static final TagKey<Item> CANE = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "cane"));
	public static final TagKey<Item> WALKER = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "walker"));
	public static final TagKey<Item> PRESERVED = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "preserved"));
	
	public static final TagKey<Item> FILTER_SWORD_CANE = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "cane_filter"));
}
