package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.chairspace.Chairspace;
import com.lying.chairspace.Chairspace.Flag;
import com.lying.entity.IParentedEntity;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCEntityTypes;

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
		if(vehicle != null && vehicle.getType() == WHCEntityTypes.WHEELCHAIR.get())
		{
			// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
			player.dismountVehicle();
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.MOUNT);
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
				chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.PARENT);
			});
	}
}
