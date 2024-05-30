package com.lying.init;

import com.google.common.base.Supplier;
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
	
	public static final RegistrySupplier<EntityType<EntityWheelchair>> WHEELCHAIR = register("wheelchair", () ->
			EntityType.Builder.create(EntityWheelchair::new, SpawnGroup.MISC).setDimensions(0.9F, 0.9F).build("wheelchair"));
    
    /**
     * TODO Entities for later versions
     * Rollator-style walker/zimmer frame
     * 	Bind to user similar to leashing or firework rocket
     * 	Limited upgrades (storage
     * 
     * Wheeled stool (see: my kitchen :P )
     */
	
	private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, Supplier<EntityType<T>> entry)
	{
		return ENTITY_TYPES.register(new Identifier(Reference.ModInfo.MOD_ID, name), entry);
	}
	
	public static void init()
	{
		ENTITY_TYPES.register();
	}
}
