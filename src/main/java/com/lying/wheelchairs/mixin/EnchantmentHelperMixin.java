package com.lying.wheelchairs.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.item.ItemWheelchair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

// FIXME Actually get this to work...
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin
{
	@Inject(method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
	private static void getWheelchairEnchants(int power, ItemStack stack, boolean treasureAllowed, final CallbackInfoReturnable<List<?>> ci)
	{
		System.out.println("Called getPossibleEntries for "+stack.getName().getString());
		if(stack.getItem() instanceof ItemWheelchair)
		{
			System.out.println("Input item is wheelchair!");
		}
	}
}
