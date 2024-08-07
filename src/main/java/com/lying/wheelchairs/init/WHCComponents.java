package com.lying.wheelchairs.init;

import com.lying.wheelchairs.VestComponent;
import com.lying.wheelchairs.reference.Reference;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

public class WHCComponents implements EntityComponentInitializer
{
	public static final ComponentKey<VestComponent> VEST_TRACKING = ComponentRegistry.getOrCreate(new Identifier(Reference.ModInfo.MOD_ID, "vest_tracking"), VestComponent.class);
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerFor(WolfEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(CatEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(ParrotEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(FoxEntity.class, VEST_TRACKING, VestComponent::new);
	}
}
