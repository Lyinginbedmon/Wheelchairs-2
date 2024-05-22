package com.lying.wheelchairs.renderer.entity.feature;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public abstract class ChairFeatureRenderer<T extends Entity>
{
	public abstract boolean shouldRender(T entity);
	
	public abstract void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float age, float yaw, float pitch, float tickDelta);
}
