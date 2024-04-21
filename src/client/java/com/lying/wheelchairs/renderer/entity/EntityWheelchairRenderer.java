package com.lying.wheelchairs.renderer.entity;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.reference.Reference;

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

public class EntityWheelchairRenderer extends EntityRenderer<EntityWheelchair>
{
	private final ItemRenderer renderItem;
	private final BlockRenderManager blockRenderManager;
	
	public EntityWheelchairRenderer(Context ctx)
	{
		super(ctx);
		this.renderItem = ctx.getItemRenderer();
		this.blockRenderManager = ctx.getBlockRenderManager();
	}
	
    protected boolean hasLabel(EntityWheelchair entity)
    {
        return super.hasLabel(entity) && (entity.shouldRenderName() || entity.hasCustomName() && entity == this.dispatcher.targetedEntity);
    }
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(EntityWheelchair entity)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void render(EntityWheelchair entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
			float h = MathHelper.lerpAngleDegrees((float)tickDelta, (float)(entity).prevBodyYaw, (float)(entity).bodyYaw);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - h));
			int color = entity.getColor();
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = ((color & 0xFF) >> 0) / 255F;
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			BlockModelRenderer modelRenderer = this.blockRenderManager.getModelRenderer();
			
			// Upgrades
			entity.getUpgrades().forEach(upgrade -> 
			{
				if(!upgrade.hasModel())
					return;
				
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(upgradeModel(upgrade.registryName())), r, g, b, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			});
			
			// Seat
			ItemStack chair = entity.getChair();
			if(chair.getItem() instanceof ItemWheelchair)
			{
				matrices.push();
					matrices.translate(-0.5F, 0F, -0.5F);
					modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout()), null, bakedModelManager.getModel(seatModel(chair.getItem())), r, g, b, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
			
			// Wheels
			renderWheels(matrices, vertexConsumers, light, entity.getLeftWheel(), entity.spinLeft, entity.getRightWheel(), entity.spinRight, entity.getEntityWorld(), entity.getId());
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
		return new ModelIdentifier(new Identifier(Reference.ModInfo.MOD_ID, "upgrade_"+upgrade.getPath()), "");
	}
}
