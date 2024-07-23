package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public interface IEntityInvoker
{
	@Invoker("adjustMovementForCollisions")
	public Vec3d adjustForCollisions(Vec3d movement);
}
