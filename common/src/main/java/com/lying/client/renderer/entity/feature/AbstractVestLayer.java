package com.lying.client.renderer.entity.feature;

import com.lying.Wheelchairs;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AbstractVestLayer<T extends LivingEntity, C extends EntityModel<T>> extends FeatureRenderer<T, C>
{
	private final Identifier mainTexture, overlayTexture;
	private final EntityModel<T> model;
	
	public AbstractVestLayer(FeatureRendererContext<T, C> context, EntityModel<T> vestModel, Identifier mainTexIn, Identifier overlayTexIn)
	{
		super(context);
		model = vestModel;
		mainTexture = mainTexIn;
		overlayTexture = overlayTexIn;
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		ItemStack vest = Wheelchairs.HANDLER.getVest(entity);
		if(vest.isEmpty() || entity.isInvisible())
			return;
		
		int color = vest.getItem() instanceof DyeableItem ? ((DyeableItem)vest.getItem()).getColor(vest) : -1;
        float r = ((color & 0xFF0000) >> 16) / 255F;
        float g = ((color & 0xFF00) >> 8) / 255F;
        float b = ((color & 0xFF) >> 0) / 255F;
		
		getContextModel().copyStateTo(model);
		model.animateModel(entity, limbAngle, limbDistance, tickDelta);
		model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		
		renderModel(model, mainTexture, matrices, vertexConsumers, light, entity, r, g, b);
		renderModel(model, overlayTexture, matrices, vertexConsumers, light, entity, 1F, 1F, 1F);
	}
}
