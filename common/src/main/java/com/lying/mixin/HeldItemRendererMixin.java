package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.Wheelchairs;
import com.lying.entity.IHeldItemRenderer;
import com.lying.item.CaneItem;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemRendererMixin
{
	// FIXME Reimplement conditional cane handle rendering
	@Inject(
		method = "renderItem(Lnet/minecraft/client/render/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", 
		at = @At("RETURN"))
	public void whc$renderCaneHandle(ArmedEntityRenderState entityState, ItemRenderState itemState, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, final CallbackInfo ci)
	{
		if(!(entityState instanceof IHeldItemRenderer))
			return;
		
		IHeldItemRenderer heldItems = (IHeldItemRenderer)entityState;
		ItemStack stack = heldItems.getHeldItem(arm);
		if(!(stack.getItem() instanceof CaneItem)) return;
		
		if(stack.getItem() instanceof CaneItem)// shouldRenderHandle(stack, itemState.getTransformation()))
		{
			CaneItem cane = (CaneItem)stack.getItem();
			ItemStack handle = cane.getHandle(stack);
			Wheelchairs.LOGGER.info(" # Rendering {} cane with {} handle", stack.getName().getString(), handle.getName().getString());
//			((HeldItemRenderer)(Object)this).renderItem(entity, handle, renderMode, leftHanded, matrices, vertexConsumers, light);
		}
	}
	
	private static boolean shouldRenderHandle(ItemStack stack, ModelTransformationMode renderMode)
	{
		switch(renderMode)
		{
			case FIRST_PERSON_LEFT_HAND:
			case FIRST_PERSON_RIGHT_HAND:
			case THIRD_PERSON_LEFT_HAND:
			case THIRD_PERSON_RIGHT_HAND:
				return true;
			case FIXED:
			case GROUND:
			case GUI:
			case HEAD:
			case NONE:
			default:
				return false;
		}
	}
}
