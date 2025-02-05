package com.lying.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.entity.IParentedEntity;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCEntityTypes;
import com.lying.utility.Chairspace;
import com.lying.utility.Chairspace.Flag;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntityMixin
{
	@Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
	private void whc$teleport(ServerWorld world, double x, double y, double z, Set<PositionFlag> flags, float yaw, float pitch, final CallbackInfoReturnable<Boolean> ci)
	{
		if(!shouldCall())
			return;
		
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		storeParentedEntities(player);
		storeChairIfNeeded(player);
	}
	
	@Inject(method = "requestTeleport(DDD)V", at = @At("HEAD"))
	private void whc$requestTeleport(double destX, double destY, double destZ, final CallbackInfo ci)
	{
		if(!shouldCall())
			return;
		
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		storeParentedEntities(player);
		storeChairIfNeeded(player);
	}
	
	@Inject(method = "requestTeleportAndDismount(DDD)V", at = @At("HEAD"), cancellable = true)
	private void whc$requestTeleportAndDismount(double destX, double destY, double destZ, final CallbackInfo ci)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		storeParentedEntities(player);
		if(storeChairIfNeeded(player))
		{
			player.requestTeleport(destX, destY, destZ);
			ci.cancel();
		}
	}
	
	private static boolean storeChairIfNeeded(ServerPlayerEntity player)
	{
		if(!player.hasVehicle()) return false;
		Entity vehicle = player.getVehicle();
		if(vehicle != null && vehicle.getType() == WHCEntityTypes.WHEELCHAIR.get())
		{
			// Store chair in Chairspace, it will be respawned by {@link ServerPlayNetworkHandlerMixin}
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			player.dismountVehicle();
			chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.MOUNT);
			
			return true;
		}
		return false;
	}
	
	private static void storeParentedEntities(ServerPlayerEntity player)
	{
		Chairspace chairs = Chairspace.getChairspace(player.getServer());
		player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player)).forEach(ent -> 
			chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_FINISH_TELEPORT.get(), Flag.PARENT));
	}
}
