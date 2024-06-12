package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.item.ItemCane;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin
{
	@Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", 
		at = @At("RETURN"))
	public void whc$renderCaneHandle(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, final CallbackInfo ci)
	{
		if(stack.getItem() instanceof ItemCane && shouldRenderHandle(stack, renderMode))
		{
			ItemCane cane = (ItemCane)stack.getItem();
			ItemStack handle = cane.getHandle(stack);
			((HeldItemRenderer)(Object)this).renderItem(entity, handle, renderMode, leftHanded, matrices, vertexConsumers, light);
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
