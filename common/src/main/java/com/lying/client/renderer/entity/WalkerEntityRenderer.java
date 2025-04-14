package com.lying.client.renderer.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.client.renderer.entity.feature.EntityFeatureRenderer;
import com.lying.client.renderer.entity.state.WalkerEntityRenderState;
import com.lying.entity.WalkerEntity;
import com.lying.item.WalkerItem;
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
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class WalkerEntityRenderer extends EntityRenderer<WalkerEntity, WalkerEntityRenderState>
{
	protected static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final ModelIdentifier CHEST_MODEL = new ModelIdentifier(Reference.ModInfo.prefix("walker_chest"), "");
	private static final double xOffset = 0.325D;
	
	protected final ItemRenderer renderItem = MinecraftClient.getInstance().getItemRenderer();
	protected final BlockRenderManager blockRenderManager;
	private final List<EntityFeatureRenderer<WalkerEntityRenderState>> featureRenderers = Lists.newArrayList();
	
	public WalkerEntityRenderer(Context context)
	{
		super(context);
		blockRenderManager = context.getBlockRenderManager();
	}
	
	public WalkerEntityRenderState createRenderState()
	{
		return new WalkerEntityRenderState();
	}
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(WalkerEntity entity)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void updateRenderState(WalkerEntity entity, WalkerEntityRenderState state, float tickDelta)
	{
		super.updateRenderState(entity, state, tickDelta);
		state.bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
		state.frame = entity.getFrame();
		state.leftWheel = entity.getLeftWheel();
		state.rightWheel = entity.getRightWheel();
		state.spinLeft = entity.spinLeft;
		state.spinRight = entity.spinRight;
		state.hasInventory = entity.hasInventory();
		state.hasParent = entity.hasParent();
		state.velocity = entity.getVelocity();
		state.casterWheelYaw = entity::casterWheelYaw;
	}
	
	public void render(WalkerEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.bodyYaw));
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			BlockModelRenderer modelRenderer = this.blockRenderManager.getModelRenderer();
			
			// Upgrades
			if(state.hasInventory)
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(CHEST_MODEL), 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Frame
			ItemStack frame = state.frame;
			if(frame.getItem() instanceof WalkerItem)
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(walkerModel(frame.getItem())), 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Wheels
			renderWheels(matrices, vertexConsumers, light, state.leftWheel, state.spinLeft, state.rightWheel, state.spinRight, state.bodyYaw, state.casterWheelYaw.apply(state.partialTick), state.world, state.entityId);
			
			this.featureRenderers.stream().filter(f -> f.shouldRender(state)).forEach(feature -> feature.render(matrices, vertexConsumers, light, state, state.partialTick));
		matrices.pop();
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, ItemStack left, float leftSpin, ItemStack right, float rightSpin, float frameYaw, float casterYaw, World world, int seed)
	{
		renderFrontWheels(matrices, renderTypeBuffer, light, frameYaw, casterYaw, left, right, leftSpin, rightSpin, xOffset, -0.15D, 0.25D, world, seed);
		renderRearWheels(matrices, renderTypeBuffer, light, left, right, leftSpin, rightSpin, xOffset, -0.1D, -0.325D, world, seed);
	}
	
	/** Renders the rotating front caster wheels */
	private void renderFrontWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, float frameYaw, float yaw, ItemStack left, ItemStack right, float leftSpin, float rightSpin, double xOffset, double yOffset, double zOffset, World world, int seed)
	{
		float scale = 0.3F;
		float thickness = 3F;
		// Right wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(xOffset, yOffset, zOffset);
			matrices.push();
				matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(frameYaw));
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rightSpin));
				matrices.push();
					matrices.scale(scale, scale, scale * thickness);
					renderItem.renderItem(right, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
				matrices.pop();
			matrices.pop();
		matrices.pop();
		
		// Left wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(-xOffset, yOffset, zOffset);
			matrices.push();
				matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(frameYaw));
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-leftSpin));
				matrices.push();
					matrices.scale(scale, scale, scale * thickness);
					renderItem.renderItem(left, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
				matrices.pop();
			matrices.pop();
		matrices.pop();
	}
	
	/** Renders the smaller fixed-angle rear wheels */
	private void renderRearWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, ItemStack left, ItemStack right, float leftSpin, float rightSpin, double xOffset, double yOffset, double zOffset, World world, int seed)
	{
		float scale = 0.2F;
		float thickness = 3F;
		// Right wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(xOffset, yOffset, zOffset);
			matrices.push();
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rightSpin));
				matrices.push();
					matrices.scale(scale, scale, scale * thickness);
					renderItem.renderItem(right, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
				matrices.pop();
			matrices.pop();
		matrices.pop();
		
		// Left wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(-xOffset, yOffset, zOffset);
			matrices.push();
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-leftSpin));
				matrices.push();
					matrices.scale(scale, scale, scale * thickness);
					renderItem.renderItem(left, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
				matrices.pop();
			matrices.pop();
		matrices.pop();
	}
	
	public static ModelIdentifier walkerModel(Item chairIn) { return new ModelIdentifier(Registries.ITEM.getId(chairIn), ""); }
}
