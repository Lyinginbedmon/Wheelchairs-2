package com.lying.client.renderer.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.client.renderer.entity.feature.EntityFeatureRenderer;
import com.lying.client.renderer.entity.state.WheelchairsRideableEntityRenderState;
import com.lying.entity.WheelchairsRideable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public abstract class WheelchairsRideableEntityRenderer<T extends WheelchairsRideable, S extends WheelchairsRideableEntityRenderState> extends EntityRenderer<T, S>
{
	protected static final MinecraftClient mc = MinecraftClient.getInstance();
	private final List<EntityFeatureRenderer<S>> featureRenderers = Lists.newArrayList();
	
	protected final ItemRenderer renderItem = MinecraftClient.getInstance().getItemRenderer();
	protected final BlockRenderManager blockRenderManager;
	
	public WheelchairsRideableEntityRenderer(Context context)
	{
		super(context);
		blockRenderManager = context.getBlockRenderManager();
	}
	
	protected final void addFeature(EntityFeatureRenderer<S> featureIn) { this.featureRenderers.add(featureIn); }
	
	protected final void renderFeatures(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		this.featureRenderers.stream().filter(f -> f.shouldRender(state)).forEach(feature -> feature.render(matrices, vertexConsumers, light, state, state.partialTick));
	}
	
	public void updateRenderState(T entity, S state, float tickDelta)
	{
		super.updateRenderState(entity, state, tickDelta);
		state.partialTick = tickDelta;
		state.world = entity.getEntityWorld();
		state.entityId = entity.getId();
		
		float f = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw);
		state.bodyYaw = clampBodyYaw(entity, f, tickDelta);
		state.yawDegrees = MathHelper.wrapDegrees(f - state.bodyYaw);
		state.pitch = entity.getLerpedPitch(tickDelta);
		state.customName = entity.getCustomName();
		
		if(!entity.hasVehicle() && entity.isAlive())
		{
			state.limbFrequency = entity.limbAnimator.getPos(tickDelta);
			state.limbAmplitudeMultiplier = entity.limbAnimator.getSpeed(tickDelta);
		}
		else
		{
			state.limbFrequency = 0.0F;
			state.limbAmplitudeMultiplier = 0.0F;
		}
		
		if(entity.getVehicle() instanceof LivingEntity livingentity)
			state.headItemAnimationProgress = livingentity.limbAnimator.getPos(tickDelta);
		else
			state.headItemAnimationProgress = state.limbFrequency;
		
		state.baseScale = entity.getScale();
		state.ageScale = entity.getScaleFactor();
		state.pose = entity.getPose();
		state.sleepingDirection = entity.getSleepingDirection();
		if (state.sleepingDirection != null)
			state.standingEyeHeight = entity.getEyeHeight(EntityPose.STANDING);
		
		state.shaking = entity.isFrozen();
		state.baby = entity.isBaby();
		state.touchingWater = entity.isTouchingWater();
		state.usingRiptide = entity.isUsingRiptide();
		state.hurt = entity.hurtTime > 0 || entity.deathTime > 0;
		state.wearingSkullType = null;
		state.wearingSkullProfile = null;
		state.headItemRenderState.clear();
		
		state.deathTime = entity.deathTime > 0 ? (float)entity.deathTime + tickDelta : 0.0F;
		state.invisibleToPlayer = state.invisible && entity.isInvisibleTo(mc.player);
		state.hasOutline = mc.hasOutline(entity);
	}
	
	private static float clampBodyYaw(LivingEntity entity, float degrees, float tickDelta)
	{
		if(entity.getVehicle() instanceof LivingEntity livingEntity)
		{
			float f = MathHelper.lerpAngleDegrees(tickDelta, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
			float yawLimit = 85.0F;
			float h = MathHelper.clamp(MathHelper.wrapDegrees(degrees - f), -yawLimit, yawLimit);
			f = degrees - h;
			if (Math.abs(h) > 50.0F)
				f += h * 0.2F;
			return f;
		}
		else
			return MathHelper.lerpAngleDegrees(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
	}
}
