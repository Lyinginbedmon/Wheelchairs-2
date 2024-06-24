package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public float forwardSpeed;
	
	@Shadow
	public float sidewaysSpeed;
	
	@Shadow
	public float upwardSpeed;
	
	@Inject(method = "tickMovement()V", at = @At("HEAD"))
	public void whc$tickMovement(final CallbackInfo ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		if(living.getType() == EntityType.PLAYER)
			IParentedEntity.getParentedEntitiesOf(living).forEach(child -> 
			{
				Vec3d movement = new Vec3d(child.sidewaysSpeed, child.upwardSpeed, child.forwardSpeed);
				child.travelControlled((PlayerEntity)living, movement);
			});
	}
}
