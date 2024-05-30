package com.lying.init;

import java.util.Iterator;

import com.lying.reference.Reference;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Enchantments registered here are able to be applied to wheelchairs in addition to their native targets
 * @author Lying
 */
public class WHCEnchantments
{
	private static final Identifier registryId = new Identifier(Reference.ModInfo.MOD_ID, "chair_enchant");
	public static final Registrar<Enchantment> REGISTRY = RegistrarManager.get(Reference.ModInfo.MOD_ID).<Enchantment>builder(registryId).build();
	public static final RegistryKey<? extends Registry<Enchantment>> KEY = REGISTRY.key();
	
	public static void register(Enchantment acc)
	{
		REGISTRY.register(new Identifier(acc.getTranslationKey()), () -> acc);
	}
	
	public static boolean isValidEnchantment(String translationKey)
	{
		Iterator<Enchantment> iterator = REGISTRY.iterator();
		while(iterator.hasNext())
		{
			Enchantment ench = iterator.next();
			if(ench.getTranslationKey().equals(translationKey))
				return true;
		}
		return false;
	}
	
	public static boolean isValidEnchantment(Enchantment ench)
	{
		return isValidEnchantment(ench.getTranslationKey());
	}
	
	public static void init()
	{
		register(Enchantments.DEPTH_STRIDER);
		register(Enchantments.FROST_WALKER);
		register(Enchantments.FIRE_PROTECTION);
		register(Enchantments.RESPIRATION);
	}
}
