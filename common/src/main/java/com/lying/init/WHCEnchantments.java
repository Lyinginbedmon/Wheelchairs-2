package com.lying.init;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.data.WHCTags;
import com.lying.reference.Reference;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class WHCEnchantments
{
	private static final List<RegistryKey<Enchantment>> WHEELCHAIR_ENCHANTS = Lists.newArrayList(); 
	
	public static final RegistryKey<Enchantment> HOLLOWED	= register("hollowed");
	public static final RegistryKey<Enchantment> SLIM		= register("slim");
	
	private static RegistryKey<Enchantment> register(String name)
	{
		return RegistryKey.of(RegistryKeys.ENCHANTMENT, Reference.ModInfo.prefix(name));
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
		markWheelchairCompatible(Enchantments.DEPTH_STRIDER);
		markWheelchairCompatible(Enchantments.FROST_WALKER);
		markWheelchairCompatible(Enchantments.FIRE_PROTECTION);
		markWheelchairCompatible(Enchantments.RESPIRATION);
	}
	
	public static Optional<RegistryEntry.Reference<Enchantment>> getFrostWalker(DynamicRegistryManager world)
	{
		return world.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FROST_WALKER.getRegistry());
	}
	
	public static Optional<RegistryEntry.Reference<Enchantment>> getFireProtection(DynamicRegistryManager world)
	{
		return world.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FIRE_PROTECTION.getRegistry());
	}
	
	public static Optional<RegistryEntry.Reference<Enchantment>> getRespiration(DynamicRegistryManager world)
	{
		return world.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.RESPIRATION.getRegistry());
	}
	
	public static Optional<RegistryEntry.Reference<Enchantment>> getDepthStrider(DynamicRegistryManager world)
	{
		return world.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.DEPTH_STRIDER.getRegistry());
	}
	
	public static Enchantment create(Identifier regName, RegistryEntryList<Item> validItems, int maxLevel, AttributeModifierSlot slot, Consumer<Enchantment.Builder> effects)
	{
		Enchantment.Builder builder = Enchantment.builder(Enchantment.definition(validItems, 5, maxLevel, Enchantment.leveledCost(5, 6), Enchantment.leveledCost(20, 6), 2, slot));
		effects.accept(builder);
		return builder.build(regName);
	}
	
	public static void bootstrap(Registerable<Enchantment> registerable)
	{
		Wheelchairs.LOGGER.info(" # Enchantments bootstrapped");
		RegistryEntryLookup<Item> itemLookup = registerable.getRegistryLookup(RegistryKeys.ITEM);
		RegistryEntryLookup<Enchantment> enchLookup = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);
		
		registerable.register(
				HOLLOWED, 
				create(
					HOLLOWED.getValue(), 
					itemLookup.getOrThrow(WHCTags.CANE), 
					1, 
					AttributeModifierSlot.HAND, 
					b -> { 
						b.addEffect(WHCEnchantmentComponentTypes.CANE_INVENTORY.get());
						b.exclusiveSet(enchLookup.getOrThrow(WHCTags.CANE_INTERACT_SET));
					}));
		registerable.register(
				SLIM, 
				create(
					SLIM.getValue(), 
					itemLookup.getOrThrow(ItemTags.SWORDS), 
					1, 
					AttributeModifierSlot.HAND, 
					b -> {
						b.addEffect(EnchantmentEffectComponentTypes.DAMAGE, new AddEnchantmentEffect(EnchantmentLevelBasedValue.constant(-2)));
						b.addEffect(WHCEnchantmentComponentTypes.CANE_STOREABLE.get());
						b.exclusiveSet(enchLookup.getOrThrow(WHCTags.CANE_INTERACT_SET));
					}));
	}
}
