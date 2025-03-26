package com.lying.client.renderer.entity;

import java.util.Optional;

import com.lying.client.renderer.entity.state.WheelchairEntityRenderState;
import com.lying.entity.WheelchairEntity;
import com.lying.item.WheelchairItem;
import com.lying.reference.Reference;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class WheelchairEntityRenderer extends WheelchairsRideableEntityRenderer<WheelchairEntity, WheelchairEntityRenderState>
{
	public WheelchairEntityRenderer(Context context)
	{
		super(context);
	}
	
	public WheelchairEntityRenderState createRenderState()
	{
		return new WheelchairEntityRenderState();
	}
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(WheelchairEntity entity)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void updateRenderState(WheelchairEntity entity, WheelchairEntityRenderState state, float tickDelta)
	{
		super.updateRenderState(entity, state, tickDelta);
		state.chair = entity.getChair();
		state.leftWheel = entity.getLeftWheel();
		state.rightWheel = entity.getRightWheel();
		state.color = entity.getColor();
		state.spinLeft = entity.spinLeft;
		state.spinRight = entity.spinRight;
		state.upgrades = entity.getUpgrades();
		state.hasParent = entity.hasParent();
		state.isFlying = entity.isGliding();
		state.velocity = entity.getVelocity();
		state.rider = entity.hasPassengers() && entity.getFirstPassenger() instanceof LivingEntity ? Optional.of((LivingEntity)entity.getFirstPassenger()) : Optional.empty();
	}
	
	public void render(WheelchairEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
			float h = 0F;
			if(state.hasParent)
				h = state.yawDegrees;
			else
				h = state.bodyYaw;
			
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - h));
			int color = state.color;
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = ((color & 0xFF) >> 0) / 255F;
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			BlockModelRenderer modelRenderer = this.blockRenderManager.getModelRenderer();
			
			// Upgrades
			state.upgrades.forEach(upgrade -> 
			{
				if(!upgrade.hasModel())
					return;
				
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(upgradeModel(upgrade.registryName())), r, g, b, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			});
			
			// Seat
			ItemStack chair = state.chair;
			if(chair.getItem() instanceof WheelchairItem)
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(seatModel(chair.getItem())), r, g, b, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Wheels
			renderWheels(matrices, vertexConsumers, light, state.leftWheel, state.spinLeft, state.rightWheel, state.spinRight, state.world, state.entityId);
			
			renderFeatures(state, matrices, vertexConsumers, light);
		matrices.pop();
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, ItemStack left, float leftSpin, ItemStack right, float rightSpin, World world, int seed)
	{
		// Right wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(0.4D, -0.5D, 0D);
			matrices.push();
				matrices.scale(1F, 1F, 1F);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rightSpin));
				renderItem.renderItem(right, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
		
		// Left wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(-0.4D, -0.5D, 0D);
			matrices.push();
				matrices.scale(1F, 1F, 1F);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-leftSpin));
				renderItem.renderItem(left, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
	}
	
	public static ModelIdentifier seatModel(Item chairIn)
	{
		Identifier registry = Registries.ITEM.getId(chairIn);
		return new ModelIdentifier(registry, "");
	}
	
	public static ModelIdentifier upgradeModel(Identifier upgrade)
	{
		return new ModelIdentifier(Reference.ModInfo.prefix("upgrade_"+upgrade.getPath()), "");
	}
}
