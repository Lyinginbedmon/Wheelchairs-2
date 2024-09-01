package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.init.WHCItems;
import com.lying.item.ItemVest;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(MobEntity.class)
public class MobEntityMixin
{
	@Inject(method = "interactWithItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("TAIL"), cancellable = true)
	private void whc$interact(PlayerEntity player, Hand hand, final CallbackInfoReturnable<ActionResult> ci)
	{
		if(ci.getReturnValue().isAccepted()) return;
		MobEntity mob = (MobEntity)(Object)this;
		ItemStack stack = player.getStackInHand(hand);
		if(!stack.isEmpty() && stack.getItem() == WHCItems.VEST.get())
		{
			ActionResult result = ((ItemVest)stack.getItem()).useOnEntity(stack, player, mob, hand);
			if(result.isAccepted())
				ci.setReturnValue(result);
		}
	}
}
