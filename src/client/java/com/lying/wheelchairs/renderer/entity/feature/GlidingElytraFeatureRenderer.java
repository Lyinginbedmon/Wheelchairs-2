package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCUpgrades;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class GlidingElytraFeatureRenderer<T extends EntityWheelchair> extends ChairFeatureRenderer<T>
{
	private static final Identifier TEXTURE = new Identifier("textures/entity/elytra.png");
	private final ElytraEntityModel<T> model;
	
	public GlidingElytraFeatureRenderer(Context ctx)
	{
		model = new ElytraEntityModel<T>(ctx.getModelLoader().getModelPart(EntityModelLayers.ELYTRA));
	}
	
	public boolean shouldRender(T entity)
	{
		return entity.hasUpgrade(WHCUpgrades.GLIDING);
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float age, float yaw, float pitch, float tickDelta)
	{
		matrices.push();
			matrices.translate(0F, 1.5F, 0.125F);
			if(entity.isFlying())
				matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90F));
			float scale = 1.75F;
			matrices.scale(scale, -scale, scale);
			// entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch
			this.model.setAngles(entity, 0, 0, age, yaw, pitch);
			VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, false);
			this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
		matrices.pop();
	}
}
