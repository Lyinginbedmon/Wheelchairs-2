package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.init.WHCEntityTypes;
import com.lying.utility.ServerBus;
import com.lying.utility.ServerEvents;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin
{
	@Inject(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"), cancellable = true)
	public void whc$getBlockBreakingSpeed(BlockState block, final CallbackInfoReturnable<Float> ci)
	{
		// Reverts a 1/5 mining speed debuff for not being on solid ground as long as your wheelchair is
		if(hasVehicle() && getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get() && getVehicle().isOnGround())
			ci.setReturnValue(ci.getReturnValue() * 5F);
	}
	
	@Inject(method = "shouldDismount()Z", at = @At("TAIL"), cancellable = true)
	public void whc$shouldDismount(final CallbackInfoReturnable<Boolean> ci)
	{
		ci.setReturnValue(ci.getReturnValue() && !ServerBus.getSeatbelt(getUuid()));
	}
	
	private boolean wasFlying = false;
	
	@Inject(method = "updatePose()V", at = @At("HEAD"))
	public void whc$updatePoseStart(final CallbackInfo ci)
	{
		wasFlying = getPose() == EntityPose.GLIDING;
	}
	
	@Inject(method = "updatePose()V", at = @At("TAIL"))
	public void whc$updatePoseEnd(final CallbackInfo ci)
	{
		if(getWorld().isClient())
			return;
		
		boolean isFlying = getPose() == EntityPose.GLIDING;
		if(wasFlying != isFlying)
		{
			PlayerEntity player = (PlayerEntity)(Object)this;
			if(isFlying)
				ServerEvents.ON_START_FLYING.invoker().onStartFlying(player);
			else
				ServerEvents.ON_STOP_FLYING.invoker().onStopFlying(player);
		}
	}
}
