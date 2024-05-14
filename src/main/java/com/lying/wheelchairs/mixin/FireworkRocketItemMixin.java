package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin
{
	@Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at = @At("HEAD"), cancellable = true)
	private void whc$use(World world, PlayerEntity user, Hand hand, final CallbackInfoReturnable<TypedActionResult<?>> ci)
	{
		System.out.println("Calling firework rocket use on server");
		if(user.hasVehicle() && user.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR)
		{
			EntityWheelchair chair = (EntityWheelchair)user.getVehicle();
			if(chair.isFallFlying())
			{
				System.out.println("S - Chair is flying");
				ci.setReturnValue(TypedActionResult.success(user.getStackInHand(hand), world.isClient()));
			}
		}
	}
}
