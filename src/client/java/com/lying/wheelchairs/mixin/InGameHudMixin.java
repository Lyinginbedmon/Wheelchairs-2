package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.init.WHCEntityTypes;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
	@Inject(method = "getRiddenEntity()Lnet/minecraft/entity/LivingEntity;", at = @At("RETURN"), cancellable = true)
	private void whc$getRiddenEntity(final CallbackInfoReturnable<LivingEntity> ci)
	{
		LivingEntity ridden = ci.getReturnValue();
		if(ridden != null && ridden.getType() == WHCEntityTypes.WHEELCHAIR)
			ci.setReturnValue(null);
	}
}
