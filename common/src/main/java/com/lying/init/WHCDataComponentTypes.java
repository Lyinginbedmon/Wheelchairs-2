package com.lying.init;

import java.util.function.UnaryOperator;

import com.lying.Wheelchairs;
import com.lying.component.type.HandleComponent;
import com.lying.component.type.SwordComponent;
import com.lying.component.type.UpgradesComponent;
import com.lying.component.type.WheelComponent;
import com.lying.reference.Reference;
import com.mojang.serialization.Codec;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;

public class WHCDataComponentTypes 
{
	private static final DeferredRegister<ComponentType<?>> TYPES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<ComponentType<WheelComponent>> LEFT_WHEEL		= register("left_wheel", builder -> builder.codec(WheelComponent.CODEC).packetCodec(WheelComponent.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<WheelComponent>> RIGHT_WHEEL		= register("right_wheel", builder -> builder.codec(WheelComponent.CODEC).packetCodec(WheelComponent.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<HandleComponent>> HANDLE			= register("handle", builder -> builder.codec(HandleComponent.CODEC).packetCodec(HandleComponent.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<SwordComponent>> SWORD			= register("sword", builder -> builder.codec(SwordComponent.CODEC).packetCodec(SwordComponent.PACKET_CODEC));
	public static final RegistrySupplier<ComponentType<Boolean>> HAS_CHEST				= register("has_chest", builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN));
	public static final RegistrySupplier<ComponentType<UpgradesComponent>> UPGRADES		= register("upgrades", builder -> builder.codec(UpgradesComponent.CODEC).packetCodec(UpgradesComponent.PACKET_CODEC));
	
	private static <T extends Object> RegistrySupplier<ComponentType<T>> register(String nameIn, UnaryOperator<ComponentType.Builder<T>> builderOperator)
	{
		tally++;
		return TYPES.register(Reference.ModInfo.prefix(nameIn), () -> builderOperator.apply(ComponentType.builder()).build());
	}
	
	public static void init()
	{
		TYPES.register();
		Wheelchairs.LOGGER.info(" # Registered {} data component types", tally);
	}
}
