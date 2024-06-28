package com.lying.wheelchairs.utility;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.entity.IParentedEntity;
import com.lying.wheelchairs.init.WHCChairspaceConditions;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.utility.Chairspace.Flag;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
	 * Fired AFTER the player confirms a teleport
	 */
	public static final Event<PlayerConfirmTeleport> AFTER_PLAYER_TELEPORT = EventFactory.createWithPhases(PlayerConfirmTeleport.class, callbacks -> (player) -> 
	{
		for(PlayerConfirmTeleport callback : callbacks)
			callback.afterTeleport(player);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface PlayerConfirmTeleport
	{
		void afterTeleport(PlayerEntity player);
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
	 */
	@FunctionalInterface
	public interface DoubleJumpEvent
	{
		void onDoubleJump(LivingEntity living);
	}
	
	public static final Event<WalkerBindEvent> ON_WALKER_BIND = EventFactory.createArrayBacked(WalkerBindEvent.class, callbacks -> (living, walker) -> 
	{
		for(WalkerBindEvent callback : callbacks)
			callback.onBindToWalker(living, walker);
	});
	
	@FunctionalInterface
	public interface WalkerBindEvent
	{
		void onBindToWalker(LivingEntity living, EntityWalker walker);
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
		Wheelchairs.LOGGER.info("Registered Chairspace event handlers");
		
		// Storing wheelchair due to rider death
		ServerLivingEntityEvents.AFTER_DEATH.register((entity,damage) -> 
		{
			if(entity.getType() != EntityType.PLAYER || entity.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(entity.getServer());
			boolean shouldDropContents = !entity.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
			
			if(entity.hasVehicle() && entity.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR)
			{
				Entity vehicle = entity.getVehicle();
				if(shouldDropContents)
					((EntityWheelchair)vehicle).dropInventory();
				chairs.storeEntityInChairspace(vehicle, entity.getUuid(), WHCChairspaceConditions.ON_RESPAWN, Flag.MOUNT);
			}
			
			entity.getWorld().getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(6D), IParentedEntity.isChildOf(entity)).forEach(ent -> 
				{
					if(ent.getType() == WHCEntityTypes.WALKER && ((EntityWalker)ent).hasInventory())
						((EntityWalker)ent).dropInventory();
					chairs.storeEntityInChairspace(ent, entity.getUuid(), WHCChairspaceConditions.ON_RESPAWN, Flag.PARENT);
				});
		});
		
		// Retrieving wheelchair when rider respawns
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, oldIsAlive) -> 
		{
			if(!newPlayer.getWorld().isClient())
				Chairspace.getChairspace(newPlayer.getServer()).reactToEvent(ServerPlayerEvents.AFTER_RESPAWN, newPlayer);
		});
		
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> 
		{
			ServerPlayerEntity player = handler.player;
			if(player == null || player.getWorld() == null)
				return;
			
			Chairspace chairs = Chairspace.getChairspace(server);
			IParentedEntity.getParentedEntitiesOf(handler.getPlayer()).forEach(ent -> chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_LOGIN, Flag.PARENT));
		});
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> 
		{
			ServerPlayerEntity player = handler.player;
			if(player == null || player.getWorld() == null)
				return;
			
			Chairspace chairs = Chairspace.getChairspace(server);
			chairs.reactToEvent(ServerPlayConnectionEvents.JOIN, player);
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
					chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_LEAVE_SPECTATOR, Flag.MOUNT);
				}
				
				player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player)).forEach(ent -> 
					{
						if(ent.getType() == WHCEntityTypes.WALKER && ((EntityWalker)ent).hasInventory())
							((EntityWalker)ent).dropInventory();
						chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_LEAVE_SPECTATOR, Flag.PARENT);
					});
			}
			
			chairs.reactToEvent(ServerBus.AFTER_PLAYER_CHANGE_GAME_MODE, player);
		});
		
		// Retrieving wheelchair when rider respawns
		ServerBus.AFTER_PLAYER_TELEPORT.register(player -> 
		{
			if(!player.getWorld().isClient())
				Chairspace.getChairspace(player.getServer()).reactToEvent(ServerBus.AFTER_PLAYER_TELEPORT, player);
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
		
		ServerBus.ON_WALKER_BIND.register((living, walker) -> 
		{
			living.getWorld().getEntitiesByType(WHCEntityTypes.WALKER, living.getBoundingBox().expand(16D), wal -> wal.isParent(living) && wal != walker).forEach(EntityWalker::clearParent);
		});
	}
}
