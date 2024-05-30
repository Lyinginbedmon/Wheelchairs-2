package com.lying.data;

import com.lying.reference.Reference;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class WHCItemTags
{
	public static final TagKey<Item> WHEEL = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "wheel"));
}
