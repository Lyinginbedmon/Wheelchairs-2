package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.VestComponent;
import com.lying.wheelchairs.init.WHCComponents;
import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.model.WolfVestModel;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class WolfVestLayer extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_wolf.png");
	private static final Identifier TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_wolf_overlay.png");
	private final WolfVestModel<WolfEntity> model;
	
	public WolfVestLayer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context)
	{
		super(context);
		model = new WolfVestModel<WolfEntity>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(WHCModelParts.WOLF_VEST));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, WolfEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		VestComponent vest = WHCComponents.VEST_TRACKING.get(entity);
		if(vest.get().isEmpty() || entity.isInvisible())
			return;
		
		ItemStack stack = vest.get();
		int color = stack.getItem() instanceof DyeableItem ? ((DyeableItem)stack.getItem()).getColor(stack) : -1;
        float r = ((color & 0xFF0000) >> 16) / 255F;
        float g = ((color & 0xFF00) >> 8) / 255F;
        float b = ((color & 0xFF) >> 0) / 255F;
		
		getContextModel().copyStateTo(model);
		model.animateModel(entity, limbAngle, limbDistance, tickDelta);
		model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		
		renderModel(model, TEXTURE, matrices, vertexConsumers, light, entity, r, g, b);
		renderModel(model, TEXTURE_OVERLAY, matrices, vertexConsumers, light, entity, 1F, 1F, 1F);
	}
}
