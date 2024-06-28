package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.init.WHCEntityTypes;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin
{
	@Inject(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"), cancellable = true)
	public void whc$getBlockBreakingSpeed(BlockState block, final CallbackInfoReturnable<Float> ci)
	{
		// Reverts a 1/5 mining speed debuff for not being on solid ground as long as your wheelchair is
		if(hasVehicle() && getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && getVehicle().isOnGround())
			ci.setReturnValue(ci.getReturnValue() * 5F);
	}
}
