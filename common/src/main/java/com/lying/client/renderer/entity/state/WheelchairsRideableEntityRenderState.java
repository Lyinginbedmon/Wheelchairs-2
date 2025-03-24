package com.lying.client.renderer.entity.state;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.lying.entity.ChairUpgrade;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WheelchairsRideableEntityRenderState extends LivingEntityRenderState
{
	public World world = null;
	public int entityId = 0;
	public float partialTick = 0F;
	
	public int color = 0xFFFFFF;
	public float spinLeft, spinRight;
	
	public ItemStack chair = ItemStack.EMPTY, leftWheel = ItemStack.EMPTY, rightWheel = ItemStack.EMPTY;
	
	public List<ChairUpgrade> upgrades = Lists.newArrayList();
	public boolean hasParent = false;
	public boolean isFlying = false;
	public boolean isGliding = false;
	public Optional<LivingEntity> rider = Optional.empty();
	public Vec3d velocity = Vec3d.ZERO;
}
