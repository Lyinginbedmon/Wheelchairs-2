package com.lying.wheelchairs.init;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WHCEntityTypes
{
	public static final EntityType<EntityWheelchair> WHEELCHAIR = register("wheelchair",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityWheelchair::new).dimensions(EntityDimensions.fixed(0.9F, 0.9F)).build());
	
	private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entry)
	{
		return Registry.register(
				Registries.ENTITY_TYPE,
				new Identifier(Reference.ModInfo.MOD_ID, name),
				entry);
	}
	
	public static void init()
	{
		FabricDefaultAttributeRegistry.register(WHEELCHAIR, LivingEntity.createLivingAttributes());
	}
}
