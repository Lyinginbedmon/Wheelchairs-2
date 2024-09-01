package com.lying.mixin;

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.lying.entity.IParentedEntity;
import com.lying.utility.ServerEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin extends WorldMixin
{
	@Shadow
	public List<ServerPlayerEntity> players = Lists.newArrayList();
	
	@Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	private void whc$tickEntity(Entity entity, final CallbackInfo ci)
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity parent = (LivingEntity)entity;
			parent.getWorld().getEntitiesByClass(LivingEntity.class, parent.getBoundingBox().expand(6D), IParentedEntity.isChildOf(parent))
				.forEach(child -> tickParented(parent, (LivingEntity & IParentedEntity)child));
		}
	}
	
	@Inject(method = "wakeSleepingPlayers()V", at = @At("HEAD"))
	private void whc$wakeSleepingPlayers(final CallbackInfo ci)
	{
		players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach(player -> ServerEvents.ON_WAKE_UP.invoker().onWakeUp(player));
	}
	
	private <T extends LivingEntity & IParentedEntity> void tickParented(LivingEntity parent, T child)
	{
		if(parent.hasPassenger(child))
			return;
		
		child.resetPosition();
		++child.age;
		IParentedEntity.updateParentingBond(child, parent);
	}
}
