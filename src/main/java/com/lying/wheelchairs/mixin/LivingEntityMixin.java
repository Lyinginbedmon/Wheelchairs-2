package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.init.WHCEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public boolean isSleeping() { return false; }
	
	@Inject(method = "canUsePortals()Z", at = @At("HEAD"), cancellable = true)
	public void canUsePortals(final CallbackInfoReturnable<Boolean> ci)
	{
		if(getType() == EntityType.PLAYER && hasVehicle() && getVehicle().getType() == WHCEntityTypes.WHEELCHAIR)
			ci.setReturnValue(!hasPassengers() && !isSleeping());
	}
}
