package com.lying.fabric.init;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

import com.lying.fabric.component.VestComponent;
import com.lying.reference.Reference;

import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;

public class WHCComponents implements EntityComponentInitializer
{
	public static final ComponentKey<VestComponent> VEST_TRACKING = ComponentRegistry.getOrCreate(Reference.ModInfo.prefix("vest_tracking"), VestComponent.class);
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerFor(WolfEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(CatEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(ParrotEntity.class, VEST_TRACKING, VestComponent::new);
		registry.registerFor(FoxEntity.class, VEST_TRACKING, VestComponent::new);
	}
}
