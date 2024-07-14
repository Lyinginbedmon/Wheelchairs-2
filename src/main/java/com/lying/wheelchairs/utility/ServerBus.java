package com.lying.wheelchairs.utility;

import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.entity.IParentedEntity;
import com.lying.wheelchairs.init.WHCChairspaceConditions;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.utility.Chairspace.Flag;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class ServerBus
{
	public static void registerEventCallbacks()
	{
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT.register(ServerEvents.EVENT_FIRST, (living, next, last) -> 
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
			
			boolean isClient = player.getWorld().isClient();
			Chairspace chairs = Chairspace.getChairspace(server);
			IParentedEntity.getParentedEntitiesOf(handler.getPlayer()).forEach(ent -> {
				ent.clearParent();
				if(ent.hasPassengers())
					return;
				else if(isClient)
					ent.discard();
				else
					chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_LOGIN, Flag.PARENT);
			});
		});
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> 
		{
			ServerPlayerEntity player = handler.player;
			if(player == null || player.getWorld() == null || player.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(server);
			chairs.reactToEvent(ServerPlayConnectionEvents.JOIN, player);
		});
		
		// Storage/retrieval due to rider being in/out of Spectator
		ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE.register(Event.DEFAULT_PHASE, (player, mode) -> 
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
			
			chairs.reactToEvent(ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE, player);
		});
		
		// Retrieving wheelchair when rider respawns
		ServerEvents.AFTER_PLAYER_TELEPORT.register(player -> 
		{
			if(!player.getWorld().isClient())
				Chairspace.getChairspace(player.getServer()).reactToEvent(ServerEvents.AFTER_PLAYER_TELEPORT, player);
		});
		
		ServerEvents.ON_START_FLYING.register(living -> 
		{
			if(living.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(living.getServer());
			living.getWorld().getEntitiesByType(WHCEntityTypes.WALKER, living.getBoundingBox().expand(6D), IParentedEntity.isChildOf(living)).forEach(ent -> 
				chairs.storeEntityInChairspace(ent, living.getUuid(), WHCChairspaceConditions.ON_STOP_FLYING, Flag.PARENT));
		});
		
		ServerEvents.ON_STOP_FLYING.register(living -> 
		{
			if(!living.getWorld().isClient())
				Chairspace.getChairspace(living.getServer()).reactToEvent(ServerEvents.ON_STOP_FLYING, living);
		});
	}
	
	private static void registerMountEvents()
	{
		// Applies and removes effects based on entering or exiting a wheelchair
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT.register(ServerEvents.EVENT_FIRST, (living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR)
				((EntityWheelchair)last).getUpgrades().forEach(upg -> upg.onStopRiding(living));
			
			if(next != null && next.getType() == WHCEntityTypes.WHEELCHAIR)
				((EntityWheelchair)next).getUpgrades().forEach(upg -> upg.onStartRiding(living));
			
			// Clear all walker bindings whenever riding status changes
			living.getWorld().getEntitiesByType(WHCEntityTypes.WALKER, living.getBoundingBox().expand(IParentedEntity.SEARCH_RANGE), wal -> wal.isParent(living)).forEach(EntityWalker::clearParent);
		});
		
		// If a player leaves their wheelchair for a different non-wheelchair mount, and the wheelchair has no items, store it in their inventory
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT.register(ServerEvents.EVENT_LAST, (living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR && last.isAlive() && living.getType() == EntityType.PLAYER)
			{
				EntityWheelchair chair = (EntityWheelchair)last;
				if(next != null && next.getType() != WHCEntityTypes.WHEELCHAIR && (!chair.hasInventory() || chair.getInventory().isEmpty()))
					chair.convertToItem((PlayerEntity)living);
			}
		});
		
		// Clear binding to any other walker when a player binds to a walker
		ServerEvents.ON_ENTITY_PARENT.register((living, walker) -> 
		{
			if(walker.getType() == WHCEntityTypes.WALKER)
				living.getWorld().getEntitiesByType(WHCEntityTypes.WALKER, living.getBoundingBox().expand(IParentedEntity.SEARCH_RANGE), wal -> wal.isParent(living) && wal != walker).forEach(EntityWalker::clearParent);
		});
	}
}
