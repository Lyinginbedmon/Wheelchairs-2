package com.lying.init;

import java.util.Iterator;

import com.lying.Wheelchairs;
import com.lying.enchant.HollowedEnchant;
import com.lying.enchant.SlimEnchantment;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
	
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENCHANTMENT);
	
	public static final RegistrySupplier<Enchantment> HOLLOWED = register("hollowed", new HollowedEnchant(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND}));
	public static final RegistrySupplier<Enchantment> SLIM = register("slim", new SlimEnchantment(Rarity.RARE));
	
	public static RegistrySupplier<Enchantment> register(String name, Enchantment acc)
	{
		return ENCHANTMENTS.register(new Identifier(Reference.ModInfo.MOD_ID, name), () -> acc);
	}
	
	public static void markWheelchairCompatible(Enchantment acc)
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
		markWheelchairCompatible(Enchantments.DEPTH_STRIDER);
		markWheelchairCompatible(Enchantments.FROST_WALKER);
		markWheelchairCompatible(Enchantments.FIRE_PROTECTION);
		markWheelchairCompatible(Enchantments.RESPIRATION);
		
		ENCHANTMENTS.register();
		Wheelchairs.LOGGER.info(" # Registered enchantments");
	}
}
