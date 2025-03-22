package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.enchantment.EnchantmentHelper;

@Mixin(value = EnchantmentHelper.class)
public class EnchantmentHelperMixin
{
	// FIXME Replace with datapack tags to attach registered enchantments to wheelchairs
//	@Inject(method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
//	private static void whc$getWheelchairEnchants(int power, ItemStack stack, boolean treasureAllowed, final CallbackInfoReturnable<List<?>> ci)
//	{
//		if(stack.getItem() instanceof ItemWheelchair)
//		{
//			ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
//			for(Enchantment enchantment : WHCEnchantments.REGISTRY)
//			{
//				if(enchantment.isTreasure() && !treasureAllowed || !enchantment.isAvailableForRandomSelection()) continue;
//				for(int i = enchantment.getMaxLevel(); i>enchantment.getMinLevel() - 1; --i)
//				{
//					if(power < enchantment.getMinPower(i) || power > enchantment.getMaxPower(i)) continue;
//					list.add(new EnchantmentLevelEntry(enchantment, i));
//					continue;
//				}
//			}
//			ci.setReturnValue(list);
//		}
//	}
}
