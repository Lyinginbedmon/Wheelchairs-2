package com.lying.wheelchairs.enchant;

import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.config.ServerConfig.SwordCaneFilter;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;

public class SlimEnchantment extends DamageEnchantment
{
	public SlimEnchantment(Rarity weight)
	{
		super(weight, 0, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
	}
	
	public boolean isCursed() { return true; }
	
    public float getAttackDamage(int level, EntityGroup group)
    {
        return -2F;
    }
    
    public int getMaxLevel() { return 1; }
    
    public boolean canAccept(Enchantment other) { return other != this; }
    
    public boolean isAvailableForEnchantedBookOffer() { return Wheelchairs.config.swordCaneFilter() == SwordCaneFilter.ENCHANT; }
    
    public boolean isAvailableForRandomSelection() { return Wheelchairs.config.swordCaneFilter() == SwordCaneFilter.ENCHANT; }
}
