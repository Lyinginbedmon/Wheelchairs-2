package com.lying.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin
{
	@Shadow
	public Profiler getProfiler() { return null; }
	
	@Shadow
	public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) { }
	
	@Shadow
	protected void tickBlockEntities() { }
}
