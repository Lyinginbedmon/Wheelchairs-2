package com.lying.client.renderer.entity.state;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.lying.entity.ChairUpgrade;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class WheelchairEntityRenderState extends WheelchairsRideableEntityRenderState
{
	public int color = 0xFFFFFF;
	public float spinLeft, spinRight;
	
	public ItemStack chair = ItemStack.EMPTY, leftWheel = ItemStack.EMPTY, rightWheel = ItemStack.EMPTY;
	
	public List<ChairUpgrade> upgrades = Lists.newArrayList();
	public boolean hasParent = false;
	public boolean isFlying = false;
	public boolean isGliding = false;
	public boolean isSneaking = false;
	public Optional<LivingEntity> rider = Optional.empty();
	public Vec3d velocity = Vec3d.ZERO;
}
