package com.lying.client.renderer.entity.state;

import java.util.function.Function;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.world.World;

public class StoolEntityRenderState extends LivingEntityRenderState
{
	public World world;
	public float partialTick;
	public int entityId;
	public int color;
	public float spin;
	public Function<Float, Float> casterWheelYaw;
}