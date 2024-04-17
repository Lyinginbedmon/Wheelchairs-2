package com.lying.wheelchairs.utility;

import com.lying.wheelchairs.init.WHCEntityTypes;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

public class ServerBus
{
	/**
	 * Fired BEFORE the player changes dimension, such as through using a portal
	 */
	public static final Event<PlayerChangeGameMode> AFTER_PLAYER_CHANGE_GAME_MODE = EventFactory.createArrayBacked(PlayerChangeGameMode.class, callbacks -> (player, mode) -> 
	{
		for(PlayerChangeGameMode callback : callbacks)
			callback.afterChangeGameMode(player, mode);
	});
	
	@FunctionalInterface
	public interface PlayerChangeGameMode
	{
		void afterChangeGameMode(PlayerEntity player, GameMode gameMode);
	}
	
	public static void registerEventCallbacks()
	{
		ServerLivingEntityEvents.AFTER_DEATH.register((entity,damage) -> 
		{
			if(entity.getType() != EntityType.PLAYER || entity.getVehicle() == null || entity.getVehicle().getType() != WHCEntityTypes.WHEELCHAIR || entity.getWorld().isClient())
				return;
			
			Entity vehicle = entity.getVehicle();
			Chairspace chairs = Chairspace.getChairspace(entity.getServer());
			chairs.storeChair(vehicle, entity.getUuid());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, oldIsAlive) -> 
		{
			if(newPlayer.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(newPlayer.getServer());
			chairs.tryRespawnChair(newPlayer.getUuid(), newPlayer);
		});
		
		ServerBus.AFTER_PLAYER_CHANGE_GAME_MODE.register((player, mode) -> 
		{
			if(player.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			if(mode == GameMode.SPECTATOR)
			{
				// Store the wheelchair
				if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR)
				{
					Entity vehicle = player.getVehicle();
					player.stopRiding();
					chairs.storeChair(vehicle, player.getUuid());
				}
			}
			else
			{
				// Respawn the wheelchair
				chairs.tryRespawnChair(player.getUuid(), player);
			}
		});
	}
}
