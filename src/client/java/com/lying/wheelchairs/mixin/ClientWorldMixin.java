package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(ClientWorld.class)
public class ClientWorldMixin
{
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
	
	private <T extends LivingEntity & IParentedEntity> void tickParented(LivingEntity parent, T child)
	{
		if(parent.hasPassenger(child))
			return;
		
		child.resetPosition();
		++child.age;
		IParentedEntity.updateParentingBond(child, parent);
	}
}
