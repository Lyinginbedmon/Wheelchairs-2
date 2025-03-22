package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.enchantment.Enchantment;

@Mixin(Enchantment.class)
public class EnchantmentMixin
{
//	@Shadow
//	@Nullable
//    protected String translationKey;
//	
//	@Inject(method = "isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", at = @At("INVOKE"), cancellable = true)
//	private void whc$onEnchantChair(ItemStack itemStack, final CallbackInfoReturnable<Boolean> ci)
//	{
//		if(itemStack.getItem() instanceof ItemWheelchair)
//			ci.setReturnValue(WHCEnchantments.isValidEnchantment(this.translationKey));
//	}
}
