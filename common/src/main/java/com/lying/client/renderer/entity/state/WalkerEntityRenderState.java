package com.lying.client.renderer.entity.state;

import java.util.function.Function;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WalkerEntityRenderState extends LivingEntityRenderState
{
	public World world = null;
	public int entityId = 0;
	public float partialTick = 0F;
	
	public int color = 0xFFFFFF;
	public float spinLeft, spinRight;
	
	public ItemStack frame = ItemStack.EMPTY, leftWheel = ItemStack.EMPTY, rightWheel = ItemStack.EMPTY;
	public boolean hasInventory = false;
	public boolean hasParent = false;
	public Vec3d velocity = Vec3d.ZERO;
	
	public Function<Float, Float> casterWheelYaw;
}
