package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin
{
	@Shadow
	public World world;
	
	@Shadow
	public World getWorld() { return null; }
	
	@Shadow
	public Entity getControllingVehicle() { return null; }
	
	@Shadow
	public boolean hasVehicle() { return false; }
	
	@Shadow
	public Entity getVehicle() { return null; }
	
	@Shadow
	public boolean hasPassengers() { return false; }
	
	@Shadow
	public boolean isRemoved() { return false; }
	
	@Shadow
	protected TeleportTarget getTeleportTarget(ServerWorld destination) { return null; }
	
	@Shadow
	public EntityType<?> getType() { return null; }
	
	@Shadow
	public void removeFromDimension() {}
}
