package com.lying.client.renderer.entity.feature;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

public abstract class EntityFeatureRenderer<T extends EntityRenderState>
{
	public abstract boolean shouldRender(T state);
	
	public abstract void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T state, float tickDelta);
}
