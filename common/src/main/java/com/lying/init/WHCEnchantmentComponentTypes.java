package com.lying.init;

import java.util.function.UnaryOperator;

import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Unit;

public class WHCEnchantmentComponentTypes
{
	private static final DeferredRegister<ComponentType<?>> COMPONENTS	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENCHANTMENT_EFFECT_COMPONENT_TYPE);
	
	/** Indicates this item (usually a cane) can store an item */
	public static final RegistrySupplier<ComponentType<Unit>> CANE_INVENTORY	= register("cane_inventory", b -> b.codec(Unit.CODEC));
	/** Indicates this item (usually a sword) can be stored in a hollowed cane */
	public static final RegistrySupplier<ComponentType<Unit>> CANE_STOREABLE	= register("cane_storeable", b -> b.codec(Unit.CODEC));
	
	private static <T> RegistrySupplier<ComponentType<T>> register(String nameIn, UnaryOperator<ComponentType.Builder<T>> builder)
	{
		return COMPONENTS.register(Reference.ModInfo.prefix(nameIn), () -> builder.apply(ComponentType.builder()).build());
	}
	
	public static void init()
	{
		COMPONENTS.register();
	}
}
