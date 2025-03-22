package com.lying.client.renderer.entity.feature;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AbstractVestLayer<C extends LivingEntityRenderState, M extends EntityModel<C>> extends FeatureRenderer<C, M>
{
	private final Identifier mainTexture, overlayTexture;
	private final M model;
	
	public AbstractVestLayer(FeatureRendererContext<C, M> context, M vestModel, Identifier mainTexIn, Identifier overlayTexIn)
	{
		super(context);
		model = vestModel;
		mainTexture = mainTexIn;
		overlayTexture = overlayTexIn;
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, C state, float limbAngle, float limbDistance)
	{
		if(state.invisibleToPlayer)
			return;
		
		// FIXME Detect service vest during rendering
//		ItemStack stack = Wheelchairs.HANDLER.getVest(entity);
//		if(stack.isEmpty())
//			return;
//		
//		int color = DyedColorComponent.getColor(stack, -1);
//        float r = ((color & 0xFF0000) >> 16) / 255F;
//        float g = ((color & 0xFF00) >> 8) / 255F;
//        float b = ((color & 0xFF) >> 0) / 255F;
//		
//        model.setAngles(state);
//		renderModel(model, mainTexture, matrices, vertexConsumers, light, state, color);
//		renderModel(model, overlayTexture, matrices, vertexConsumers, light, state, -1);
	}
}
