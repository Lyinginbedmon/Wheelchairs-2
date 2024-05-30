package com.lying.utility;

import org.jetbrains.annotations.Nullable;

import com.lying.Wheelchairs;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class ServerBus
{
	public static final Identifier EVENT_FIRST = new Identifier(Reference.ModInfo.MOD_ID, "first");
	public static final Identifier EVENT_LAST = new Identifier(Reference.ModInfo.MOD_ID, "last");
	
	/**
	 * Fired AFTER the player changes gamemode
	 */
	public static final Event<PlayerChangeGameMode> AFTER_PLAYER_CHANGE_GAME_MODE = EventFactory.createLoop(PlayerChangeGameMode.class);
	
	@FunctionalInterface
	public interface PlayerChangeGameMode
	{
		void afterChangeGameMode(PlayerEntity player, GameMode gameMode);
	}
	
	/**
	 * Fired AFTER a living entity changes its mount/vehicle
	 */
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT_START = EventFactory.createLoop(LivingChangeMount.class);
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT_END = EventFactory.createLoop(LivingChangeMount.class);
	
	@FunctionalInterface
	public interface LivingChangeMount
	{
		void afterChangeMount(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount);
	}
	
	/**
	 * Fired when a wheelchair tries to jump in midair 
	 */
	public static final Event<DoubleJumpEvent> ON_DOUBLE_JUMP = EventFactory.createLoop(DoubleJumpEvent.class);
	
	/**
	 * Fired when a living entity jumps in midair
	 *
	 */
	@FunctionalInterface
	public interface DoubleJumpEvent
	{
		void onDoubleJump(LivingEntity living);
	}
	
	public static void registerEventCallbacks()
	{
		ServerBus.AFTER_LIVING_CHANGE_MOUNT_START.register((living, next, last) -> 
		{
			Wheelchairs.LOGGER.info("Mount changed: "+living.getName().getString()+", "+(last == null ? "NULL" : last.getName().getString())+" -> "+(next == null ? "NULL" : next.getName().getString()));
		});
		
		registerChairspaceEvents();
		registerMountEvents();
	}
	
	public static void invokeMountChange(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount)
	{
		ServerBus.AFTER_LIVING_CHANGE_MOUNT_START.invoker().afterChangeMount(living, nextMount, lastMount);
		ServerBus.AFTER_LIVING_CHANGE_MOUNT_END.invoker().afterChangeMount(living, nextMount, lastMount);
	}
	
	/**
	 * Registers event handling related to Chairspace
	 */
	private static void registerChairspaceEvents()
	{
		Wheelchairs.LOGGER.info("Registered Chairspace handling");
		
		// Storing wheelchair due to rider death
		EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource damage) -> 
		{
			if(entity.getType() != EntityType.PLAYER || entity.getVehicle() == null || entity.getVehicle().getType() != WHCEntityTypes.WHEELCHAIR.get() || entity.getWorld().isClient())
				return EventResult.pass();
			
			Entity vehicle = entity.getVehicle();
			if(!entity.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
				((EntityWheelchair)vehicle).dropInventory();
			Chairspace.getChairspace(entity.getServer()).storeChair(vehicle, entity.getUuid());
			return EventResult.pass();
		});
		
		// Retrieving wheelchair when rider respawns
		PlayerEvent.PLAYER_RESPAWN.register((ServerPlayerEntity newPlayer, boolean conqueredEnd) -> 
		{
			if(newPlayer.getWorld().isClient())
				return;
			else
				Chairspace.getChairspace(newPlayer.getServer()).tryRespawnChair(newPlayer.getUuid(), newPlayer);
		});
		
		// Storage/retrieval due to rider being in/out of Spectator
		ServerBus.AFTER_PLAYER_CHANGE_GAME_MODE.register((player, mode) -> 
		{
			if(player.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			if(mode == GameMode.SPECTATOR)
			{
				// Store the wheelchair
				if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get())
				{
					Entity vehicle = player.getVehicle();
					player.stopRiding();
					chairs.storeChair(vehicle, player.getUuid());
				}
			}
			// Respawn the wheelchair
			else
				chairs.tryRespawnChair(player.getUuid(), player);
		});
	}
	
	private static void registerMountEvents()
	{
		// Applies and removes effects based on entering or exiting a wheelchair
		ServerBus.AFTER_LIVING_CHANGE_MOUNT_START.register((living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR.get())
				((EntityWheelchair)last).getUpgrades().forEach(upg -> upg.onStopRiding(living));
			
			if(next != null && next.getType() == WHCEntityTypes.WHEELCHAIR.get())
				((EntityWheelchair)next).getUpgrades().forEach(upg -> upg.onStartRiding(living));
		});
		
		// If a player leaves their wheelchair for a different non-wheelchair mount, and the wheelchair has no items, store it in their inventory
		ServerBus.AFTER_LIVING_CHANGE_MOUNT_END.register((living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR.get() && last.isAlive() && living.getType() == EntityType.PLAYER)
			{
				EntityWheelchair chair = (EntityWheelchair)last;
				if(next != null && next.getType() != WHCEntityTypes.WHEELCHAIR.get() && (!chair.hasInventory() || chair.getInventory().isEmpty()))
					chair.convertToItem((PlayerEntity)living);
			}
		});
	}
}
