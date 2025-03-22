package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderer;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
	@Shadow
	private ItemModelManager itemModelManager;
	
	// FIXME Reimplement custom item model usage
//	@ModifyVariable(
//		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", 
//		at = @At("HEAD"),
//		ordinal = 0)
//	public BakedModel whc$renderItem(BakedModel model2, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model)
//	{
//		Identifier replacement = WHCItemsClient.getExtraModelIfAny(stack.getItem(), renderMode);
//		if(replacement != null)
//		{
//			AccessorItemModelManager manager = (AccessorItemModelManager)this.itemModelManager;
//			return manager.modelGetter().apply(replacement);
//		}
//		return model;
//	}
}
