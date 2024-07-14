package com.lying.wheelchairs.init;

import com.lying.wheelchairs.reference.Reference;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WHCSoundEvents
{
	private static final Identifier ID_SEATBELT_ON = new Identifier(Reference.ModInfo.MOD_ID, "seatbelt_on");
	public static final SoundEvent SEATBELT_ON = SoundEvent.of(ID_SEATBELT_ON);
	
	private static final Identifier ID_SEATBELT_OFF = new Identifier(Reference.ModInfo.MOD_ID, "seatbelt_off");
	public static final SoundEvent SEATBELT_OFF = SoundEvent.of(ID_SEATBELT_OFF);
	
	private static final Identifier ID_SWORD_DRAW = new Identifier(Reference.ModInfo.MOD_ID, "cane_sword_draw");
	public static final SoundEvent SWORD_DRAW = SoundEvent.of(ID_SWORD_DRAW);
	
	public static void init()
	{
		Registry.register(Registries.SOUND_EVENT, ID_SEATBELT_ON, SEATBELT_ON);
		Registry.register(Registries.SOUND_EVENT, ID_SEATBELT_OFF, SEATBELT_OFF);
		Registry.register(Registries.SOUND_EVENT, ID_SWORD_DRAW, SWORD_DRAW);
	}
}
