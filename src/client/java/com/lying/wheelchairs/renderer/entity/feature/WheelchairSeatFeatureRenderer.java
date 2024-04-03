package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.model.entity.ModelWheelchair;
import com.lying.wheelchairs.renderer.entity.EntityWheelchairRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class WheelchairSeatFeatureRenderer extends FeatureRenderer<EntityWheelchair, ModelWheelchair<EntityWheelchair>>
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
	
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
		
		matrices.push();
			float h = MathHelper.lerpAngleDegrees((float)tickDelta, (float)(living).prevBodyYaw, (float)(living).bodyYaw);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - h));
			if(chair.getItem() instanceof ItemWheelchair)
			{
				BakedModelManager bakedModelManager = blockRenderManager.getModels().getModelManager();
				ModelIdentifier model = EntityWheelchairRenderer.seatModel(chair.getItem());
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					blockRenderManager.getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid()), null, bakedModelManager.getModel(model), r, g, b, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
		matrices.pop();
	}
}