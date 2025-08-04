package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.WheelchairElytraModel;
import com.lying.client.renderer.entity.state.WheelchairEntityRenderState;
import com.lying.init.WHCChairUpgrades;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;

public class WheelchairElytraFeatureRenderer<T extends WheelchairEntityRenderState> extends EntityFeatureRenderer<T>
{
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/equipment/wings/elytra.png");
	private final WheelchairElytraModel<T> model;
	
	public WheelchairElytraFeatureRenderer(Context ctx)
	{
		model = new WheelchairElytraModel<T>(ctx.getEntityModels().getModelPart(WHCModelParts.UPGRADE_ELYTRA));
	}
	
	public boolean shouldRender(T state)
	{
		return state.upgrades.contains(WHCChairUpgrades.GLIDING.get());
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T state, float tickDelta)
	{
		matrices.push();
			matrices.translate(0F, state.isFlying ? 1.5F : 1.3F, 0.125F + (state.isFlying ? 0F : 0.1F));
			float scale = state.isFlying ? 1.75F : 0.8F;
			matrices.scale(scale, -scale, scale);
			this.model.setAngles(state, 0, 0, state.age, state.yawDegrees, state.pitch);
			VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(getTexture(state)), false);
			this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);
		matrices.pop();
	}
	
	public Identifier getTexture(T state)
	{
		if(state.rider.isPresent())
		{
			SkinTextures skinTextures;
			final LivingEntity livingEntity = state.rider.get();
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
