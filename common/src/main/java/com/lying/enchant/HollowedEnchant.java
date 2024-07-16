package com.lying.enchant;

import com.lying.item.ItemCane;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class HollowedEnchant extends Enchantment
{
	// FIXME Prevent Hollowed being applied to non-cane items
	public HollowedEnchant(Rarity rarity, EnchantmentTarget target, EquipmentSlot[] slotTypes)
	{
		super(rarity, target, slotTypes);
	}
	
	public boolean isAcceptableItem(ItemStack stack) { return stack.getItem() instanceof ItemCane; }
    
    public int getMaxLevel() { return 1; }
}
