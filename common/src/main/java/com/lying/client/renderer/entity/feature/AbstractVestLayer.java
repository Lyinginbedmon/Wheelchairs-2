package com.lying.client.renderer.entity.feature;

import com.lying.entity.IServiceVestHolder;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
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
		
		IServiceVestHolder extension = (IServiceVestHolder)state;
		if(!extension.hasVest())
			return;
		
		ItemStack stack = extension.getVest();
		int color = DyedColorComponent.getColor(stack, -6265536);
        model.setAngles(state);
		renderModel(model, mainTexture, matrices, vertexConsumers, light, state, color);
		renderModel(model, overlayTexture, matrices, vertexConsumers, light, state, -1);
	}
}
