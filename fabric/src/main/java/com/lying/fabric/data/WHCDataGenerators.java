package com.lying.fabric.data;

import com.lying.init.WHCEnchantments;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class WHCDataGenerators implements DataGeneratorEntrypoint
{
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
	{
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(WHCRecipeProvider::new);
		pack.addProvider(WHCItemTagsProvider::new);
		pack.addProvider(WHCEnchantmentProvider::new);
		pack.addProvider(WHCEnchantmentTagProvider::new);
	}
	
	public void buildRegistry(RegistryBuilder builder)
	{
		builder.addRegistry(RegistryKeys.ENCHANTMENT, WHCEnchantments::bootstrap);
	}
}
