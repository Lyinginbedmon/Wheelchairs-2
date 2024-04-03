package com.lying.wheelchairs.renderer.entity;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.model.entity.ModelWheelchair;
import com.lying.wheelchairs.renderer.entity.feature.WheelchairSeatFeatureRenderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

// FIXME Convert to EntityRenderer rendering discrete block model from chair item
public class EntityWheelchairRenderer extends LivingEntityRenderer<EntityWheelchair, ModelWheelchair<EntityWheelchair>>
{
	private final ItemRenderer renderItem;
	
	public EntityWheelchairRenderer(Context ctx)
	{
		super(ctx, new ModelWheelchair<EntityWheelchair>(ctx.getModelLoader().getModelPart(WHCModelParts.WHEELCHAIR)), 0.5F);
		this.renderItem = ctx.getItemRenderer();
		
		this.addFeature(new WheelchairSeatFeatureRenderer(this));
	}
	
    protected boolean hasLabel(EntityWheelchair entity)
    {
        return super.hasLabel(entity) && (entity.shouldRenderName() || entity.hasCustomName() && entity == this.dispatcher.targetedEntity);
    }
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(EntityWheelchair entity)
	{
		ItemStack chair = entity.getChair();
		if(chair.getItem() instanceof ItemWheelchair)
			return ((ItemWheelchair)chair.getItem()).textureMain();
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void render(EntityWheelchair entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		ItemStack chair = entity.getChair();
		if(chair.getItem() instanceof ItemWheelchair)
			super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		
		ItemStack wheelLeft = entity.getLeftWheel();
		ItemStack wheelRight = entity.getRightWheel();
		
		matrices.push();
			float h = MathHelper.lerpAngleDegrees((float)tickDelta, (float)(entity).prevBodyYaw, (float)(entity).bodyYaw);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - h));
			renderWheels(matrices, vertexConsumers, light, 0, wheelLeft, wheelRight, entity.getEntityWorld(), entity.getId());
		matrices.pop();
	}
	
	private void renderWheels(MatrixStack matrices, VertexConsumerProvider renderTypeBuffer, int light, float spin, ItemStack left, ItemStack right, World world, int seed)
	{
		// Right wheel
		matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
			matrices.translate(0.4D, -0.5D, 0D);
			matrices.push();
				matrices.scale(1F, 1F, 1F);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-spin));
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
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-spin));
				renderItem.renderItem(left, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, renderTypeBuffer, world, seed);
			matrices.pop();
		matrices.pop();
	}
}
