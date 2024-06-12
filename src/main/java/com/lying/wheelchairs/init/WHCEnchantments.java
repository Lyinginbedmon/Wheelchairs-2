package com.lying.wheelchairs.init;

import java.util.Map.Entry;

import com.lying.wheelchairs.enchant.HollowedEnchant;
import com.lying.wheelchairs.enchant.SlimEnchantment;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class WHCEnchantments
{
	public static final RegistryKey<Registry<Enchantment>> KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "chair_enchant"));
	public static final Registry<Enchantment> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();
	
	public static final Enchantment HOLLOWED = new HollowedEnchant(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
	public static final Enchantment SLIM = new SlimEnchantment(Rarity.RARE);
	
	public static void register(String name, Enchantment acc)
	{
		Registry.register(Registries.ENCHANTMENT, new Identifier(Reference.ModInfo.MOD_ID, name), acc);
	}
	
	/**
	 * Enchantments registered here are able to be applied to wheelchairs in addition to their native targets
	 * @author Lying
	 */
	public static void markWheelchairCompatible(Enchantment acc)
	{
		Registry.register(REGISTRY, acc.getTranslationKey(), acc);
	}
	
	/** Returns true if the given enchantment is compatible with wheelchairs */
	public static boolean isValidEnchantment(String translationKey)
	{
		for(Entry<RegistryKey<Enchantment>, Enchantment> entry : REGISTRY.getEntrySet())
			if(entry.getValue().getTranslationKey().equals(translationKey))
				return true;
		return false;
	}
	
	/** Returns true if the given enchantment is compatible with wheelchairs */
	public static boolean isValidEnchantment(Enchantment ench)
	{
		return isValidEnchantment(ench.getTranslationKey());
	}
	
	public static void init()
	{
		register("hollowed", HOLLOWED);
		register("slim", SLIM);
		
		markWheelchairCompatible(Enchantments.DEPTH_STRIDER);
		markWheelchairCompatible(Enchantments.FROST_WALKER);
		markWheelchairCompatible(Enchantments.FIRE_PROTECTION);
		markWheelchairCompatible(Enchantments.RESPIRATION);
	}
}
