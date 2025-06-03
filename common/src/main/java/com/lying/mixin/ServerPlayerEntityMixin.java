package com.lying.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.Wheelchairs;
import com.lying.chairspace.Chairspace;
import com.lying.chairspace.Chairspace.Flag;
import com.lying.entity.IParentedEntity;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCEntityTypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends EntityMixin
{
	@Shadow
	public void stopRiding() { }
	
	@Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At("HEAD"))
	private void whc$teleportTo(TeleportTarget teleportTarget, final CallbackInfoReturnable<ServerPlayerEntity> ci)
	{
		Optional<Entity> vehicleOpt = hasVehicle() ? Optional.of(getVehicle()) : recallVehicle();
		Wheelchairs.LOGGER.info(" # teleportTo triggered in ServerPlayerEntity, vehicle: {}", vehicleOpt.isPresent());
		if(isRemoved())
			return;
		
		Chairspace chairs = Chairspace.getChairspace(getServer());
		vehicleOpt.ifPresent(vehicle -> 
		{
			if(vehicle.getType() == WHCEntityTypes.WHEELCHAIR.get())
			{
				// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
				stopRiding();
				chairs.storeEntityInChairspace(vehicle, getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.MOUNT);
			}
		});
		
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player))
			.forEach(ent -> chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.PARENT));
	}
}
