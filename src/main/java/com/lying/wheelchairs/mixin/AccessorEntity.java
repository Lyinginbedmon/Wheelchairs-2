package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public interface AccessorEntity
{
	@Invoker("adjustMovementForCollisions")
	public Vec3d adjustToPreventCollision(Vec3d movement);
}
