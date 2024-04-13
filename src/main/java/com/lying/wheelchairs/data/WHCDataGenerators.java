package com.lying.wheelchairs.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class WHCDataGenerators implements DataGeneratorEntrypoint
{
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
	{
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(WHCRecipeProvider::new);
		pack.addProvider(WHCItemTags::new);
	}
}
