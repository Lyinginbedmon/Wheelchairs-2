package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends EntityMixin
{
//	@Shadow
//	public void stopRiding() { }
//	
//	@Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At("HEAD"))
//	private void whc$teleportTo(TeleportTarget teleportTarget, final CallbackInfoReturnable<ServerPlayerEntity> ci)
//	{
//		if(isRemoved())
//			return;
//		
//		Chairspace chairs = Chairspace.getChairspace(getServer());
//		Entity vehicle = getVehicle();
//		if(vehicle != null && vehicle.getType() == WHCEntityTypes.WHEELCHAIR.get())
//		{
//			// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
//			stopRiding();
//			chairs.storeEntityInChairspace(vehicle, getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.MOUNT);
//		}
//		
//		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
//		player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player))
//		.forEach(ent -> chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.PARENT));
//	}
//	
//	@Inject(method = "requestTeleportAndDismount(DDD)V", at = @At("HEAD"), cancellable = true)
//	private void whc$requestTeleportAndDismount(double destX, double destY, double destZ, final CallbackInfo ci)
//	{
//		ci.cancel();
//		
//		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
//	}
//	
//	@Inject(method = "requestTeleport(DDD)V", at = @At("HEAD"))
//	private void whc$requestTeleport(double destX, double destY, double destZ, final CallbackInfo ci)
//	{
//		Wheelchairs.LOGGER.info(" # Teleport requested");
//		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
//		player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player))
//		.forEach(ent -> 
//		{
//			Wheelchairs.LOGGER.info(" # # Storing parented entity {}", ent.getName().getString());
//			chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.PARENT);
//		});
//	}
}
