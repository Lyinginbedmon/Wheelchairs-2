package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.WHCTags;
import com.lying.init.WHCEnchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.EnchantmentTags;

public class WHCEnchantmentTagProvider extends FabricTagProvider.EnchantmentTagProvider
{
	public WHCEnchantmentTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	public String getName() { return "Wheelchairs enchantment tags"; }
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(EnchantmentTags.CURSE).add(WHCEnchantments.SLIM);
		getOrCreateTagBuilder(EnchantmentTags.TRADEABLE).add(WHCEnchantments.SLIM);
		
		getOrCreateTagBuilder(EnchantmentTags.NON_TREASURE).add(WHCEnchantments.HOLLOWED);
		getOrCreateTagBuilder(EnchantmentTags.IN_ENCHANTING_TABLE).add(WHCEnchantments.HOLLOWED);
		
		getOrCreateTagBuilder(EnchantmentTags.TOOLTIP_ORDER).add(WHCEnchantments.HOLLOWED, WHCEnchantments.SLIM);
		
		getOrCreateTagBuilder(WHCTags.CANE_INTERACT_SET).add(WHCEnchantments.HOLLOWED, WHCEnchantments.SLIM);
	}
}
