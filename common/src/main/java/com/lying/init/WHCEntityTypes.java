package com.lying.init;

import com.google.common.base.Supplier;
import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class WHCEntityTypes
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENTITY_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<EntityType<EntityWheelchair>> WHEELCHAIR = register("wheelchair", () ->
			EntityType.Builder.create(EntityWheelchair::new, SpawnGroup.MISC).setDimensions(0.7F, 0.9F).build("wheelchair"));
    
	public static final RegistrySupplier<EntityType<EntityWalker>> WALKER = register("walker", () ->
			EntityType.Builder.create(EntityWalker::new, SpawnGroup.MISC).setDimensions(0.7F, 0.9F).build("walker"));
	
	public static final RegistrySupplier<EntityType<EntityStool>> STOOL = register("wheeled_stool", () ->
			EntityType.Builder.create(EntityStool::new, SpawnGroup.MISC).setDimensions(0.7F, 0.9F).build("wheeled_stool"));
	
	private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, Supplier<EntityType<T>> entry)
	{
		++tally;
		return ENTITY_TYPES.register(new Identifier(Reference.ModInfo.MOD_ID, name), entry);
	}
	
	public static void init()
	{
		ENTITY_TYPES.register();
		Wheelchairs.LOGGER.info(" # Registered "+tally+" entity types");
	}
}
