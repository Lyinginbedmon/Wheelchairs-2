package com.lying.init;

import java.util.function.Supplier;

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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class WHCEntityTypes
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENTITY_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<EntityType<EntityWheelchair>> WHEELCHAIR = register("wheelchair", () -> 
		EntityType.Builder.<EntityWheelchair>create(EntityWheelchair::new, SpawnGroup.MISC).dimensions(0.7F, 0.9F).build(keyOf("wheelchair")));
	
	public static final RegistrySupplier<EntityType<EntityWalker>> WALKER = register("walker", () -> 
		EntityType.Builder.<EntityWalker>create(EntityWalker::new, SpawnGroup.MISC).dimensions(0.7F, 0.9F).build(keyOf("walker")));
	
	public static final RegistrySupplier<EntityType<EntityStool>> STOOL = register("stool", () -> 
		EntityType.Builder.<EntityStool>create(EntityStool::new, SpawnGroup.MISC).dimensions(0.7F, 0.9F).build(keyOf("stool")));
	
	private static RegistryKey<EntityType<?>> keyOf(String nameIn) { return RegistryKey.of(RegistryKeys.ENTITY_TYPE, Reference.ModInfo.prefix(nameIn)); }
	
	private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, Supplier<EntityType<T>> entry)
	{
		++tally;
		return ENTITY_TYPES.register(Reference.ModInfo.prefix(name), entry);
	}
	
	public static void init()
	{
		ENTITY_TYPES.register();
		Wheelchairs.LOGGER.info(" # Registered " + tally + " entity types");
	}
}
