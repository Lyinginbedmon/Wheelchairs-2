package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.WheelchairElytraModel;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCUpgrades;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;

public class WheelchairElytraFeatureRenderer<T extends EntityWheelchair> extends EntityFeatureRenderer<T>
{
	private static final Identifier TEXTURE = Identifier.of("textures/entity/elytra.png");
	private final WheelchairElytraModel<T> model;
	
	public WheelchairElytraFeatureRenderer(Context ctx)
	{
		model = new WheelchairElytraModel<T>(ctx.getEntityModels().getModelPart(WHCModelParts.UPGRADE_ELYTRA));
	}
	
	public boolean shouldRender(T entity)
	{
		return entity.hasUpgrade(WHCUpgrades.GLIDING.get());
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float age, float yaw, float pitch, float tickDelta)
	{
		matrices.push();
			matrices.translate(0F, entity.isFlying() ? 1.5F : 1.3F, 0.125F + (entity.isFlying() ? 0F : 0.1F));
			float scale = entity.isFlying() ? 1.75F : 0.8F;
			matrices.scale(scale, -scale, scale);
			this.model.setAngles(entity, 0, 0, age, yaw, pitch);
			// FIXME Restore elytra upgrade rendering
//			VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(getTexture(entity)), false, false);
//			this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
		matrices.pop();
	}
	
	public Identifier getTexture(T entity)
	{
		if(entity.getFirstPassenger() != null && entity.getFirstPassenger() instanceof LivingEntity)
		{
			SkinTextures skinTextures;
			LivingEntity livingEntity = (LivingEntity)entity.getFirstPassenger();
			AbstractClientPlayerEntity playerEntity;
			return livingEntity instanceof AbstractClientPlayerEntity ? 
						((skinTextures = (playerEntity = (AbstractClientPlayerEntity)livingEntity).getSkinTextures()).elytraTexture() != null ? 
							skinTextures.elytraTexture() : 
							(skinTextures.capeTexture() != null && playerEntity.isPartVisible(PlayerModelPart.CAPE) ? 
								skinTextures.capeTexture() : 
								TEXTURE)) : 
						TEXTURE;
		}
		return TEXTURE;
	}
}
