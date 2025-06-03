package com.lying.mixin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.utility.ServerBus;
import com.lying.utility.ServerEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Used only to implement the living-change-mount event.<br>
 * This is then used in {@link ServerBus} to manage rider attribute modifiers applied by wheelchairs.
 */
@Mixin(Entity.class)
public abstract class EntityMixin
{
	// Manages event calls so that stopRiding doesn't cause an event whilst startRiding is executing
	private int calls = 0;
	
	/** The previous vehicle that was being ridden */
	private Entity originalVehicle;
	
	private static final Long DETACH_TIME_OUT	= 10L;
	protected Optional<Entity> latestVehicle = Optional.empty();
	protected long detachTime = Long.MAX_VALUE;
	
	@Shadow
	protected static final int GLIDING_FLAG_INDEX = 7;
	
	@Shadow
	public boolean isRemoved() { return false; }
	
	@Shadow
	public MinecraftServer getServer() { return null; }
	
	@Shadow
	public UUID getUuid() { return null; }
	
	@Shadow
	public boolean hasVehicle() { return false; }
	
	@Shadow
	public Entity getVehicle() { return null; }
	
	@Shadow
	public World getWorld() { return null; }
	
	@Shadow
	public EntityType<?> getType() { return null; }
	
	@Shadow
	public Box getBoundingBox() { return null; }
	
	@Shadow
	protected boolean getFlag(int index) { return false; }
	
	@Shadow
	public EntityPose getPose() { return EntityPose.STANDING; }
	
	@Shadow
	public DataTracker getDataTracker() { return null; }
	
	private boolean shouldCall() { return getWorld() != null && !getWorld().isClient(); }
	
	protected long currentTime() { return getWorld().getTime(); }
	
	protected Optional<Entity> recallVehicle()
	{
		if(latestVehicle.isEmpty())
			return latestVehicle;
		
		long time = currentTime();
		return time >= detachTime ? (time - detachTime) < DETACH_TIME_OUT ? latestVehicle : Optional.empty() : Optional.empty();
	}
	
	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"))
	private void whc$startRidingHead(Entity entity, boolean force, final CallbackInfoReturnable<Boolean> ci)
	{
		if(!shouldCall())
			return;
		
		if(calls == 0)
			originalVehicle = getVehicle();
		
		calls++;
	}
	
	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	private void whc$startRidingTail(Entity entity, boolean force, final CallbackInfoReturnable<Boolean> ci)
	{
		if(shouldCall())
		{
			Entity ent = (Entity)(Object)this;
			if(--calls <= 0 && ent instanceof LivingEntity)
			{
				if(getVehicle() != originalVehicle)
					ServerEvents.AFTER_LIVING_CHANGE_MOUNT_END.invoker().afterChangeMount((LivingEntity)ent, getVehicle(), originalVehicle);
				
				originalVehicle = null;
				calls = 0;
			}
		}
	}
	
	@Inject(method = "stopRiding()V", at = @At("HEAD"))
	private void whc$stopRiding(final CallbackInfo ci)
	{
		if(hasVehicle())
		{
			latestVehicle = Optional.of(getVehicle());
			detachTime = currentTime();
		}
		
		if(!shouldCall() || !hasVehicle())
			return;
		else if(calls == 0)
			originalVehicle = getVehicle();
	}
	
	@Inject(method = "stopRiding()V", at = @At("TAIL"))
	private void whc$stopRidingTail(final CallbackInfo ci)
	{
		if(!shouldCall())
			return;
		else if(calls == 0 && originalVehicle != null)
		{
			Entity ent = (Entity)(Object)this;
			if(ent instanceof LivingEntity)
				ServerEvents.AFTER_LIVING_CHANGE_MOUNT_END.invoker().afterChangeMount((LivingEntity)ent, null, originalVehicle);
			
			originalVehicle = null;
		}
	}
}
