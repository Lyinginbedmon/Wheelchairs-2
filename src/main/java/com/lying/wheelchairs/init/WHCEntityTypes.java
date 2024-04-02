package com.lying.wheelchairs.init;

import com.lying.wheelchairs.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WHCEntityTypes
{
//	public static final EntityType<EntityFoxFire> FOX_FIRE = register("fox_fire",
//			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityFoxFire::new).dimensions(EntityDimensions.fixed(0.2F, 0.2F)).build());
	
	private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entry)
	{
		return Registry.register(
				Registries.ENTITY_TYPE,
				new Identifier(Reference.ModInfo.MOD_ID, name),
				entry);
	}
	
	public static void init()
	{
//		FabricDefaultAttributeRegistry.register(TRICKSY_FOX, EntityTricksyFox.createMobAttributes());
	}
}
