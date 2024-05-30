package com.lying.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.init.WHCEnchantments;
import com.lying.item.ItemWheelchair;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

@Mixin(Enchantment.class)
public class EnchantmentMixin
{
	@Shadow
	@Nullable
    protected String translationKey;
	
	@Inject(method = "isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", at = @At("INVOKE"), cancellable = true)
	private void whc$onEnchantChair(ItemStack itemStack, final CallbackInfoReturnable<Boolean> ci)
	{
		if(itemStack.getItem() instanceof ItemWheelchair)
			ci.setReturnValue(WHCEnchantments.isValidEnchantment(this.translationKey));
	}
}
