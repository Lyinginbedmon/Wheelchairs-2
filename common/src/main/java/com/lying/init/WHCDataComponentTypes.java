package com.lying.init;

import java.util.List;
import java.util.function.UnaryOperator;

import com.lying.reference.Reference;
import com.mojang.serialization.Codec;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class WHCDataComponentTypes 
{
	private static final DeferredRegister<ComponentType<?>> TYPES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
	
	public static final RegistrySupplier<ComponentType<ItemStack>> LEFT_WHEEL	= register("left_wheel", builder -> builder.codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<ItemStack>> RIGHT_WHEEL	= register("right_wheel", builder -> builder.codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<ItemStack>> HANDLE		= register("handle", builder -> builder.codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<ItemStack>> SWORD		= register("sword", builder -> builder.codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<Boolean>> HAS_CHEST		= register("has_chest", builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN));
	public static final RegistrySupplier<ComponentType<List<Identifier>>> UPGRADES	= register("upgrades", builder -> builder.codec(Identifier.CODEC.listOf()).packetCodec(Identifier.PACKET_CODEC.collect(PacketCodecs.toList())));
	
	private static <T extends Object> RegistrySupplier<ComponentType<T>> register(String nameIn, UnaryOperator<ComponentType.Builder<T>> builderOperator)
	{
		return TYPES.register(Reference.ModInfo.prefix(nameIn), () -> builderOperator.apply(ComponentType.builder()).build());
	}
	
	public static void init()
	{
		TYPES.register();
	}
}
