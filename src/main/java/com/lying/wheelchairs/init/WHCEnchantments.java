package com.lying.wheelchairs.init;

import java.util.Map.Entry;

import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
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
	public static final RegistryKey<Registry<Enchantment>> KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "chair_enchant"));
	public static final Registry<Enchantment> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();
	
	public static void register(Enchantment acc)
	{
		Registry.register(REGISTRY, acc.getTranslationKey(), acc);
	}
	
	public static boolean isValidEnchantment(String translationKey)
	{
		for(Entry<RegistryKey<Enchantment>, Enchantment> entry : REGISTRY.getEntrySet())
			if(entry.getValue().getTranslationKey().equals(translationKey))
				return true;
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
