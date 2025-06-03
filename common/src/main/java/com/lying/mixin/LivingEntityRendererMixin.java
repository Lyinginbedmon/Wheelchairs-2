package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.entity.IHeldItemRenderer;
import com.lying.entity.IServiceVestHolder;
import com.lying.item.VestItem;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin
{
	@Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
	private void whc$updateRenderState(LivingEntity entity, LivingEntityRenderState state, float g, final CallbackInfo ci)
	{
		if(state instanceof IServiceVestHolder && VestItem.isValidMobForVest(entity))
			((IServiceVestHolder)state).setVest(VestItem.getVest(entity));
		
		if(state instanceof IHeldItemRenderer)
		{
			IHeldItemRenderer renderer = (IHeldItemRenderer)state;
			renderer.setHeldItem(entity.getMainHandStack(), entity.getMainArm());
			renderer.setHeldItem(entity.getOffHandStack(), entity.getMainArm().getOpposite());
		}
	}
}
