package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	
	@Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	private void whc$tickEntity(Entity entity, final CallbackInfo ci)
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity parent = (LivingEntity)entity;
			parent.getWorld().getEntitiesByClass(LivingEntity.class, parent.getBoundingBox().expand(6D), IParentedEntity.isChildOf(parent))
				.forEach(child -> tickParented(parent, child));
		}
	}
	
	private void tickParented(LivingEntity parent, LivingEntity child)
	{
		if(parent.hasPassenger(child))
			return;
		
		child.resetPosition();
		++child.age;
		IParentedEntity.updateParentingBond(child, parent);
	}
}
