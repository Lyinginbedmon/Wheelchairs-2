package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.entity.IFlyingMount;
import com.lying.client.network.FlyingMountRocketPacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixinClient
{
	@Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at = @At("HEAD"), cancellable = true)
	private void whc$use(World world, PlayerEntity user, Hand hand, final CallbackInfoReturnable<TypedActionResult<?>> ci)
	{
		if(user.hasVehicle() && user.getVehicle() instanceof IFlyingMount)
		{
			IFlyingMount mount = (IFlyingMount)user.getVehicle();
			if(mount.canUseRocketNow())
			{
				FlyingMountRocketPacket.send(hand);
				ci.setReturnValue(TypedActionResult.success(user.getStackInHand(hand), true));
			}
		}
	}
}
