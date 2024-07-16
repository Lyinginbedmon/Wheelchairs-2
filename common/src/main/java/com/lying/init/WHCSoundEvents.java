package com.lying.init;

import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WHCSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SOUND_EVENT);
	
	private static final Identifier ID_SEATBELT_ON = new Identifier(Reference.ModInfo.MOD_ID, "seatbelt_on");
	public static final RegistrySupplier<SoundEvent> SEATBELT_ON = register(ID_SEATBELT_ON);
	
	private static final Identifier ID_SEATBELT_OFF = new Identifier(Reference.ModInfo.MOD_ID, "seatbelt_off");
	public static final RegistrySupplier<SoundEvent> SEATBELT_OFF = register(ID_SEATBELT_OFF);
	
	private static final Identifier ID_SWORD_DRAW = new Identifier(Reference.ModInfo.MOD_ID, "cane_sword_draw");
	public static final RegistrySupplier<SoundEvent> SWORD_DRAW = register(ID_SWORD_DRAW);
	
	private static RegistrySupplier<SoundEvent> register(Identifier name)
	{
		return SOUND_EVENTS.register(name, () -> SoundEvent.of(name));
	}
	
	public static void init()
	{
		SOUND_EVENTS.register();
	}
}
