package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.model.entity.ModelWheelchair;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class WheelchairSeatFeatureRenderer extends FeatureRenderer<EntityWheelchair, ModelWheelchair<EntityWheelchair>>
{
	public WheelchairSeatFeatureRenderer(FeatureRendererContext<EntityWheelchair, ModelWheelchair<EntityWheelchair>> context)
	{
		super(context);
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityWheelchair living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		ItemStack chair = living.getChair();
		if(living.isInvisible() || !(chair.getItem() instanceof ItemWheelchair))
			return;
		
		int color = living.getColor();
		float r = ((color & 0xFF0000) >> 16) / 255F;
		float g = ((color & 0xFF00) >> 8) / 255F;
		float b = ((color & 0xFF) >> 0) / 255F;
		WheelchairSeatFeatureRenderer.render(getContextModel(), getContextModel(), ((ItemWheelchair)chair.getItem()).textureOverlay(), matrices, vertexConsumers, light, living, limbAngle, limbDistance, age, headYaw, headPitch, tickDelta, r, g, b);
	}
}