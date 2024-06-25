package com.lying.wheelchairs.renderer.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.item.ItemWalker;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.feature.EntityFeatureRenderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class EntityWalkerRenderer extends EntityRenderer<EntityWalker>
{
	private static final ModelIdentifier CHEST_MODEL = new ModelIdentifier(new Identifier(Reference.ModInfo.MOD_ID, "walker_chest"), "");
	private static final double xOffset = 0.325D;
	private final List<EntityFeatureRenderer<EntityWalker>> featureRenderers = Lists.newArrayList();
	
	private final ItemRenderer renderItem;
	private final BlockRenderManager blockRenderManager;
	
	public EntityWalkerRenderer(Context ctx)
	{
		super(ctx);
		this.renderItem = ctx.getItemRenderer();
		this.blockRenderManager = ctx.getBlockRenderManager();
	}
	
	protected final void addFeature(EntityFeatureRenderer<EntityWalker> featureIn) { this.featureRenderers.add(featureIn); }
	
    protected boolean hasLabel(EntityWalker entity)
    {
        return super.hasLabel(entity) && (entity.shouldRenderName() || entity.hasCustomName() && entity == this.dispatcher.targetedEntity);
    }
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(EntityWalker entity)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void render(EntityWalker entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
			float frameYaw = MathHelper.lerpAngleDegrees((float)tickDelta, (float)(entity).prevFrameYaw, (float)(entity).frameYaw);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - frameYaw));
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			BlockModelRenderer modelRenderer = this.blockRenderManager.getModelRenderer();
			
			// Upgrades
			if(entity.hasInventory())
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(CHEST_MODEL), 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Seat
			ItemStack chair = entity.getFrame();
			if(chair.getItem() instanceof ItemWalker)
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(walkerModel(chair.getItem())), 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Wheels
			renderWheels(matrices, vertexConsumers, light, entity.getLeftWheel(), entity.spinLeft, entity.getRightWheel(), entity.spinRight, entity.getEntityWorld(), entity.getId());
			
			float age = (float)entity.age + tickDelta;
			float renderYaw = MathHelper.lerpAngleDegrees((float)tickDelta, (float)entity.prevHeadYaw, (float)entity.headYaw);
			float renderPitch = MathHelper.lerp((float)tickDelta, (float)entity.prevPitch, (float)entity.getPitch());
			this.featureRenderers.forEach(feature -> { if(feature.shouldRender(entity)) feature.render(matrices, vertexConsumers, light, entity, age, renderYaw, renderPitch, tickDelta); });
		matrices.pop();
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, ItemStack left, float leftSpin, ItemStack right, float rightSpin, World world, int seed)
	{
		renderWheels(matrices, renderTypeBuffer, light, left, right, leftSpin, rightSpin, xOffset, -0.15D, 0.25D, 0.3F, world, seed);
		renderWheels(matrices, renderTypeBuffer, light, left, right, leftSpin, rightSpin, xOffset, -0.1D, -0.325D, 0.2F, world, seed);
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, ItemStack left, ItemStack right, float leftSpin, float rightSpin, double xOffset, double yOffset, double zOffset, float scale, World world, int seed)
	{
		float thickness = 3F;
		// Right wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(xOffset, yOffset, zOffset);
			matrices.push();
				matrices.scale(scale * thickness, scale, scale);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rightSpin));
				renderItem.renderItem(right, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
		
		// Left wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(-xOffset, yOffset, zOffset);
			matrices.push();
			matrices.scale(scale * thickness, scale, scale);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-leftSpin));
				renderItem.renderItem(left, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
	}
	
	public static ModelIdentifier walkerModel(Item chairIn) { return new ModelIdentifier(Registries.ITEM.getId(chairIn), ""); }
}
