package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.client.WheelchairsClient;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinClient
{
	@Inject(method = "shouldDismount()Z", at = @At("TAIL"), cancellable = true)
	public void whc$shouldDismount(final CallbackInfoReturnable<Boolean> ci)
	{
		ci.setReturnValue(ci.getReturnValue() && !WheelchairsClient.SEATBELT_ON);
	}
}
