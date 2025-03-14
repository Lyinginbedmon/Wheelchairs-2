package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.enchant.HollowedEnchant;
import com.lying.enchant.SlimEnchantment;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Rarity;

public class WHCEnchantments
{
	private static final DeferredRegister<Enchantment> ENCHANTS	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENCHANTMENT);
	
	private static final List<RegistryKey<Enchantment>> WHEELCHAIR_ENCHANTS = Lists.newArrayList(); 
	
	public static final RegistrySupplier<Enchantment> HOLLOWED	= register("hollowed", new HollowedEnchant(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND}));
	public static final RegistrySupplier<Enchantment> SLIM		= register("slim", new SlimEnchantment(Rarity.RARE));
	
	private static RegistrySupplier<Enchantment> register(String name, Enchantment acc)
	{
		return ENCHANTS.register(prefix(name), () -> acc);
	}
	
	/**
	 * Enchantments registered here are able to be applied to wheelchairs in addition to their native targets
	 * @author Lying
	 */
	public static void markWheelchairCompatible(RegistryKey<Enchantment> acc)
	{
		WHEELCHAIR_ENCHANTS.add(acc);
	}
	
	/** Returns true if the given enchantment is compatible with wheelchairs */
	public static boolean isValidEnchantment(String translationKey)
	{
		return WHEELCHAIR_ENCHANTS.stream().anyMatch(a -> a.getValue().toString().equalsIgnoreCase(translationKey));
	}
	
	public static void init()
	{
		ENCHANTS.register();
		
		markWheelchairCompatible(Enchantments.DEPTH_STRIDER);
		markWheelchairCompatible(Enchantments.FROST_WALKER);
		markWheelchairCompatible(Enchantments.FIRE_PROTECTION);
		markWheelchairCompatible(Enchantments.RESPIRATION);
	}
}
