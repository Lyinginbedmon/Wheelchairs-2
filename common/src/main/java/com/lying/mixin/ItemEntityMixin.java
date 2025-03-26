package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.init.WHCChairUpgrades;
import com.lying.item.WheelchairItem;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
public class ItemEntityMixin
{
	@Shadow
	public ItemStack getStack() { return ItemStack.EMPTY; }
	
	@Inject(method = "isFireImmune()Z", at = @At("HEAD"), cancellable = true)
	public void whc$isChairFireImmune(final CallbackInfoReturnable<Boolean> ci)
	{
		if(getStack().getItem() instanceof WheelchairItem && WheelchairItem.hasUpgrade(getStack(), WHCChairUpgrades.NETHERITE.get()))
			ci.setReturnValue(true);
	}
}
