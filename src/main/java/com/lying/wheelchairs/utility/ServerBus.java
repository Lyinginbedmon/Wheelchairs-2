package com.lying.wheelchairs.utility;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCChairspaceConditions;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
	public static final Event<PlayerChangeGameMode> AFTER_PLAYER_CHANGE_GAME_MODE = EventFactory.createWithPhases(PlayerChangeGameMode.class, callbacks -> (player, mode) -> 
	{
		for(PlayerChangeGameMode callback : callbacks)
			callback.afterChangeGameMode(player, mode);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface PlayerChangeGameMode
	{
		void afterChangeGameMode(PlayerEntity player, GameMode gameMode);
	}
	
	/**
	 * Fired AFTER a living entity changes its mount/vehicle
	 */
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT = EventFactory.createWithPhases(LivingChangeMount.class, callbacks -> (living, nextMount, lastMount) -> 
	{
		for(LivingChangeMount callback : callbacks)
			callback.afterChangeMount(living, nextMount, lastMount);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface LivingChangeMount
	{
		void afterChangeMount(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount);
	}
	
	public static final Event<DoubleJumpEvent> ON_DOUBLE_JUMP = EventFactory.createArrayBacked(DoubleJumpEvent.class, callbacks -> (living) -> 
	{
		for(DoubleJumpEvent callback : callbacks)
			callback.onDoubleJump(living);
	});
	
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
		ServerBus.AFTER_LIVING_CHANGE_MOUNT.register(EVENT_FIRST, (living, next, last) -> 
		{
			Wheelchairs.LOGGER.info("Mount changed: "+living.getName().getString()+", "+(last == null ? "NULL" : last.getName().getString())+" -> "+(next == null ? "NULL" : next.getName().getString()));
		});
		
		registerChairspaceEvents();
		registerMountEvents();
		
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, oldIsAlive) ->
		{
			if(!newPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
			{
				PlayerInventory oldInv = oldPlayer.getInventory();
				PlayerInventory newInv = newPlayer.getInventory();
				for(int i=0; i<oldInv.size(); i++)
				{
					ItemStack stack = oldInv.getStack(i);
					if(!stack.isEmpty() && stack.isIn(WHCItemTags.PRESERVED))
						newInv.setStack(i, stack.copy());
				}
			}
		});
	}
	
	/**
	 * Registers event handling related to Chairspace
	 */
	private static void registerChairspaceEvents()
	{
		Wheelchairs.LOGGER.info("Registered Chairspace handling");
		
		// Storing wheelchair due to rider death
		ServerLivingEntityEvents.AFTER_DEATH.register((entity,damage) -> 
		{
			if(entity.getType() != EntityType.PLAYER || entity.getVehicle() == null || entity.getVehicle().getType() != WHCEntityTypes.WHEELCHAIR || entity.getWorld().isClient())
				return;
			
			Entity vehicle = entity.getVehicle();
			if(!entity.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
				((EntityWheelchair)vehicle).dropInventory();
			Chairspace.getChairspace(entity.getServer()).storeEntityInChairspace(vehicle, entity.getUuid(), WHCChairspaceConditions.ON_RESPAWN);
		});
		
		// Retrieving wheelchair when rider respawns
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, oldIsAlive) -> 
		{
			if(newPlayer.getWorld().isClient())
				return;
			else
				Chairspace.getChairspace(newPlayer.getServer()).respawnForCondition(newPlayer.getUuid(), newPlayer, WHCChairspaceConditions.ON_RESPAWN);
		});
		
		// Storage/retrieval due to rider being in/out of Spectator
		ServerBus.AFTER_PLAYER_CHANGE_GAME_MODE.register(Event.DEFAULT_PHASE, (player, mode) -> 
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
					chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_GAMEMODE_CHANGE);
				}
			}
			// Respawn the wheelchair
			else
				chairs.respawnForCondition(player.getUuid(), player, WHCChairspaceConditions.ON_GAMEMODE_CHANGE);
		});
	}
	
	private static void registerMountEvents()
	{
		// Applies and removes effects based on entering or exiting a wheelchair
		ServerBus.AFTER_LIVING_CHANGE_MOUNT.register(EVENT_FIRST, (living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR)
				((EntityWheelchair)last).getUpgrades().forEach(upg -> upg.onStopRiding(living));
			
			if(next != null && next.getType() == WHCEntityTypes.WHEELCHAIR)
				((EntityWheelchair)next).getUpgrades().forEach(upg -> upg.onStartRiding(living));
		});
		
		// If a player leaves their wheelchair for a different non-wheelchair mount, and the wheelchair has no items, store it in their inventory
		ServerBus.AFTER_LIVING_CHANGE_MOUNT.register(EVENT_LAST, (living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR && last.isAlive() && living.getType() == EntityType.PLAYER)
			{
				EntityWheelchair chair = (EntityWheelchair)last;
				if(next != null && next.getType() != WHCEntityTypes.WHEELCHAIR && (!chair.hasInventory() || chair.getInventory().isEmpty()))
					chair.convertToItem((PlayerEntity)living);
			}
		});
	}
}
