package com.lying.client.renderer.entity;

import com.lying.client.renderer.entity.state.StoolEntityRenderState;
import com.lying.entity.StoolEntity;
import com.lying.init.WHCItems;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class StoolEntityRenderer extends EntityRenderer<StoolEntity, StoolEntityRenderState>
{
	private static final ModelIdentifier MODEL = new ModelIdentifier(Reference.ModInfo.prefix("wheeled_stool"), "");
	private final ItemRenderer renderItem = MinecraftClient.getInstance().getItemRenderer();
	private final BlockRenderManager blockRenderManager;
	
	public StoolEntityRenderer(Context context)
	{
		super(context);
		this.blockRenderManager = context.getBlockRenderManager();
	}
	
	public StoolEntityRenderState createRenderState()
	{
		return new StoolEntityRenderState();
	}
	
	protected boolean hasLabel(StoolEntity entity, double squaredDistanceToCamera) { return false; }
	
	public void updateRenderState(StoolEntity entity, StoolEntityRenderState state, float tickDelta)
	{
		super.updateRenderState(entity, state, tickDelta);
		state.world = entity.getWorld();
		state.partialTick = tickDelta;
		state.entityId = entity.getId();
		state.bodyYaw = MathHelper.lerpAngleDegrees((float)tickDelta, (float)(entity).prevBodyYaw, (float)(entity).bodyYaw);
		state.color = entity.getColor();
		state.spin = entity.spin;
		state.casterWheelYaw = entity::casterWheelYaw;
	}
	
	public void render(StoolEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.bodyYaw));
			int color = state.color;
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = ((color & 0xFF) >> 0) / 255F;
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			BlockModelRenderer modelRenderer = this.blockRenderManager.getModelRenderer();
			
			// Seat
			matrices.push();
				matrices.translate(-0.5F, 0F, -0.5F);
				modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(MODEL), r, g, b, light, OverlayTexture.DEFAULT_UV);
			matrices.pop();
			
			// Wheels
			renderWheels(matrices, vertexConsumers, light, state.spin, state.casterWheelYaw.apply(state.partialTick), state.bodyYaw, state.world, state.entityId);
		matrices.pop();
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, float spin, float yaw, float frameYaw, World world, int seed)
	{
		matrices.push();
			matrices.translate(0D, 0.1D, 0D);
			
			matrices.push();
				matrices.translate(0.275D, 0D, 0D);
				renderWheel(matrices, renderTypeBuffer, light, spin, yaw, frameYaw, world, seed);
			matrices.pop();
			
			matrices.push();
				matrices.translate(-0.275D, 0D, 0D);
				renderWheel(matrices, renderTypeBuffer, light, spin, yaw, frameYaw, world, seed);
			matrices.pop();
			
			matrices.push();
				matrices.translate(0D, 0D, 0.275D);
				renderWheel(matrices, renderTypeBuffer, light, spin, yaw, frameYaw, world, seed);
			matrices.pop();
			
			matrices.push();
				matrices.translate(0D, 0D, -0.2750D);
				renderWheel(matrices, renderTypeBuffer, light, spin, yaw, frameYaw, world, seed);
			matrices.pop();
		matrices.pop();
	}
	
	private void renderWheel(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, float spin, float yaw, float frameYaw, World world, int seed)
	{
		float scale = 0.2F;
		float thickness = 3F;
		matrices.push();
			matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(frameYaw));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-spin));
			matrices.push();
				matrices.scale(scale, scale, scale * thickness);
				renderItem.renderItem(WHCItems.WHEEL_IRON.get().getDefaultStack(), ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
	}
}
