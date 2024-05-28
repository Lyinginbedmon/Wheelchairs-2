package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.lying.wheelchairs.init.WHCItemsClient;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
	@Shadow
	private ItemModels models;
	
	// FIXME Ensure that crutch model is exchanged when not viewed in third-person, ala tridents
	// Old method, doesn't work, due to be removed
	@Inject(
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", 
		at = @At("HEAD"),
		cancellable = true)
	public void whc$renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, final CallbackInfo ci)
	{
		if(WHCItemsClient.CRUTCH_MAP.containsKey(stack.getItem()) && shouldReplaceCrutch(renderMode))
			model = this.models.getModelManager().getModel(WHCItemsClient.CRUTCH_MAP.get(stack.getItem()));
	}
	
	// New method, doesn't work, but for different reasons
	@ModifyVariable(
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", 
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/render/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
	public void whc$renderItem2(Args args)
	{
		ItemStack stack = args.get(0);
		ModelTransformationMode renderMode = args.get(1);
		if(WHCItemsClient.CRUTCH_MAP.containsKey(stack.getItem()) && shouldReplaceCrutch(renderMode))
			args.set(7, this.models.getModelManager().getModel(WHCItemsClient.CRUTCH_MAP.get(stack.getItem())));
	}
	
	private static boolean shouldReplaceCrutch(ModelTransformationMode renderMode)
	{
		return !(
				renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND ||
				renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
	}
}
