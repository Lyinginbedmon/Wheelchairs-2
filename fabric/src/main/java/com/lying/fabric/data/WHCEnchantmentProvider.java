package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.init.WHCEnchantments;
import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public class WHCEnchantmentProvider extends FabricDynamicRegistryProvider
{
	public WHCEnchantmentProvider(FabricDataOutput output)
	{
		super(output, CompletableFuture.supplyAsync(BuiltinRegistries::createWrapperLookup));
	}
	
	public String getName() { return "Wheelchairs enchantments"; }
	
	protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries)
	{
		WHCEnchantments.bootstrap(createRegisterable(registries, entries));
	}
	
	private static <T> Registerable<T> createRegisterable(RegistryWrapper.WrapperLookup registries, Entries entries)
	{
		return new Registerable<>()
			{
				public RegistryEntry.Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
					return (RegistryEntry.Reference<T>) entries.add(key, value);
				}
				
				public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
					return registries.getOrThrow(registryRef);
				}
			};
	}
}
