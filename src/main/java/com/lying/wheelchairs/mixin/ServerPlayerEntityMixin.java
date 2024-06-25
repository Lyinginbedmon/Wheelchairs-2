package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.entity.IParentedEntity;
import com.lying.wheelchairs.init.WHCChairspaceConditions;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.utility.Chairspace;
import com.lying.wheelchairs.utility.Chairspace.Flag;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
	@Inject(method = "requestTeleportAndDismount(DDD)V", at = @At("HEAD"), cancellable = true)
	private void whc$requestTeleportAndDismount(double destX, double destY, double destZ, final CallbackInfo ci)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		
		Entity vehicle = player.getVehicle();
		if(vehicle != null && vehicle.getType() == WHCEntityTypes.WHEELCHAIR)
		{
			// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
			player.dismountVehicle();
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT, Flag.MOUNT);
			player.requestTeleport(destX, destY, destZ);
			ci.cancel();
		}
	}
	
	@Inject(method = "requestTeleport(DDD)V", at = @At("HEAD"))
	private void whc$requestTeleport(double destX, double destY, double destZ, final CallbackInfo ci)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		if(player.getWorld().isClient())
			return;
		
		player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player))
			.forEach(ent -> 
			{
				Chairspace chairs = Chairspace.getChairspace(player.getServer());
				chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT, Flag.PARENT);
			});
	}
}
