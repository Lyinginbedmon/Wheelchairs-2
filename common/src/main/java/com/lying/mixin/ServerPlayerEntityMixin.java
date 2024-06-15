package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.init.WHCEntityTypes;
import com.lying.utility.Chairspace;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
	@Inject(method = "requestTeleportAndDismount(DDD)V", at = @At("HEAD"), cancellable = true)
	private void whc$requestTeleportAndDismount(double destX, double destY, double destZ, final CallbackInfo ci)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		Entity vehicle = player.getVehicle();
		if(vehicle != null && vehicle.getType() == WHCEntityTypes.WHEELCHAIR.get())
		{
			// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			player.dismountVehicle();
			chairs.storeChair(vehicle, player.getUuid());
			
			player.requestTeleport(destX, destY, destZ);
			
			ci.cancel();
		}
	}
}
