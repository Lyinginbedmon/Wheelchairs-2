package com.lying.wheelchairs.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.init.WHCEnchantments;
import com.lying.wheelchairs.item.ItemWheelchair;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;

@Mixin(value = EnchantmentHelper.class)
public class EnchantmentHelperMixin
{
	@Inject(method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
	private static void whc$getWheelchairEnchants(int power, ItemStack stack, boolean treasureAllowed, final CallbackInfoReturnable<List<?>> ci)
	{
		if(stack.getItem() instanceof ItemWheelchair)
		{
			ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
			for(Enchantment enchantment : WHCEnchantments.REGISTRY)
			{
				if(enchantment.isTreasure() && !treasureAllowed || !enchantment.isAvailableForRandomSelection()) continue;
				for(int i = enchantment.getMaxLevel(); i>enchantment.getMinLevel() - 1; --i)
				{
					if(power < enchantment.getMinPower(i) || power > enchantment.getMaxPower(i)) continue;
					list.add(new EnchantmentLevelEntry(enchantment, i));
					continue;
				}
			}
			ci.setReturnValue(list);
		}
	}
}
