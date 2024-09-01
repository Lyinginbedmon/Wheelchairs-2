package com.lying.utility;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.Wheelchairs;
import com.lying.data.WHCItemTags;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.entity.IParentedEntity;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.utility.Chairspace.Flag;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT_START.register((living, next, last) -> 
		{
			Wheelchairs.LOGGER.info("Mount changed: "+living.getName().getString()+", "+(last == null ? "NULL" : last.getName().getString())+" -> "+(next == null ? "NULL" : next.getName().getString()));
		});
		
		registerChairspaceEvents();
		registerMountEvents();
		
		PlayerEvent.PLAYER_CLONE.register((ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean wonGame) -> 
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
	
	public static void invokeMountChange(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount)
	{
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT_START.invoker().afterChangeMount(living, nextMount, lastMount);
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT_END.invoker().afterChangeMount(living, nextMount, lastMount);
	}
	
	/**
	 * Registers event handling related to Chairspace
	 */
	private static void registerChairspaceEvents()
	{
		Wheelchairs.LOGGER.info("Registered Chairspace event handlers");
		
		// Storing wheelchair due to rider death
		EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource damage) -> 
		{
			if(entity.getType() != EntityType.PLAYER || entity.getWorld().isClient())
				return EventResult.pass();
			
			Chairspace chairs = Chairspace.getChairspace(entity.getServer());
			boolean shouldDropContents = !entity.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
			
			if(entity.hasVehicle() && entity.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get())
			{
				Entity vehicle = entity.getVehicle();
				if(shouldDropContents)
					((EntityWheelchair)vehicle).dropInventory();
				chairs.storeEntityInChairspace(vehicle, entity.getUuid(), WHCChairspaceConditions.ON_RESPAWN.get(), Flag.MOUNT);
			}
			
			entity.getWorld().getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(IParentedEntity.SEARCH_RANGE), IParentedEntity.isChildOf(entity)).forEach(ent -> 
				{
					if(ent.getType() == WHCEntityTypes.WALKER && ((EntityWalker)ent).hasInventory())
						((EntityWalker)ent).dropInventory();
					chairs.storeEntityInChairspace(ent, entity.getUuid(), WHCChairspaceConditions.ON_RESPAWN.get(), Flag.PARENT);
				});
			return EventResult.pass();
		});
		
		// Retrieving wheelchair when rider respawns
		PlayerEvent.PLAYER_RESPAWN.register((ServerPlayerEntity newPlayer, boolean conqueredEnd) -> 
		{
			if(!newPlayer.getWorld().isClient())
				Chairspace.getChairspace(newPlayer.getServer()).reactToEvent(PlayerEvent.PLAYER_RESPAWN, newPlayer);
		});
		
		PlayerEvent.PLAYER_QUIT.register((player) -> 
		{
			if(player == null || player.getWorld() == null)
				return;
			
			boolean isClient = player.getWorld().isClient();
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			IParentedEntity.getParentedEntitiesOf(player).forEach(ent -> {
				ent.clearParent();
				if(ent.hasPassengers())
					return;
				else if(isClient)
					ent.discard();
				else
					chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_LOGIN.get(), Flag.PARENT);
			});
		});
		
		PlayerEvent.PLAYER_JOIN.register((player) -> 
		{
			if(player == null || player.getWorld() == null || player.getWorld().isClient())
				return;
			
			Chairspace chairs = Chairspace.getChairspace(player.getServer());
			chairs.reactToEvent(PlayerEvent.PLAYER_JOIN, player);
		});
		
		// Storage/retrieval due to rider being in/out of Spectator
		ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE.register((player, mode) -> 
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
					chairs.storeEntityInChairspace(vehicle, player.getUuid(), WHCChairspaceConditions.ON_LEAVE_SPECTATOR.get(), Flag.MOUNT);
				}
				
				player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6D), IParentedEntity.isChildOf(player)).forEach(ent -> 
					{
						if(ent.getType() == WHCEntityTypes.WALKER && ((EntityWalker)ent).hasInventory())
							((EntityWalker)ent).dropInventory();
						chairs.storeEntityInChairspace(ent, player.getUuid(), WHCChairspaceConditions.ON_LEAVE_SPECTATOR.get(), Flag.PARENT);
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
			living.getWorld().getEntitiesByType(WHCEntityTypes.WALKER.get(), living.getBoundingBox().expand(IParentedEntity.SEARCH_RANGE), IParentedEntity.isChildOf(living)).forEach(ent -> 
				chairs.storeEntityInChairspace(ent, living.getUuid(), WHCChairspaceConditions.ON_STOP_FLYING.get(), Flag.PARENT));
		});
		
		ServerEvents.ON_STOP_FLYING.register(living -> 
		{
			if(!living.getWorld().isClient())
				Chairspace.getChairspace(living.getServer()).reactToEvent(ServerEvents.ON_STOP_FLYING, living);
		});
		
		EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource damageSource) -> 
		{
			if(ItemVest.isValidMobForVest(entity) && !ItemVest.getVest(entity).isEmpty())
			{
				UUID ownerID = ItemVest.getVestedMobOwner(entity);
				if(ownerID == null)
					return EventResult.pass();
				
				entity.setHealth(1F);
				Chairspace chairs = Chairspace.getChairspace(entity.getServer());
				chairs.storeEntityInChairspace(entity, ownerID, WHCChairspaceConditions.ON_WAKE_UP.get());
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
		
		ServerEvents.ON_WAKE_UP.register(player -> 
		{
			if(!player.getWorld().isClient())
				Chairspace.getChairspace(player.getServer()).reactToEvent(ServerEvents.ON_WAKE_UP, player);
		});
	}
	
	private static void registerMountEvents()
	{
		// Applies and removes effects based on entering or exiting a wheelchair
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT_START.register((living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR.get())
				((EntityWheelchair)last).getUpgrades().forEach(upg -> upg.onStopRiding(living));
			
			if(next != null && next.getType() == WHCEntityTypes.WHEELCHAIR.get())
				((EntityWheelchair)next).getUpgrades().forEach(upg -> upg.onStartRiding(living));
			
			// Clear all walker bindings whenever riding status changes
			IParentedEntity.clearParentedEntities(living, null);
		});
		
		// If a player leaves their wheelchair for a different non-wheelchair mount, and the wheelchair has no items, store it in their inventory
		ServerEvents.AFTER_LIVING_CHANGE_MOUNT_END.register((living, next, last) -> 
		{
			if(last != null && last.getType() == WHCEntityTypes.WHEELCHAIR.get() && last.isAlive() && living.getType() == EntityType.PLAYER)
			{
				EntityWheelchair chair = (EntityWheelchair)last;
				if(next != null && next.getType() != WHCEntityTypes.WHEELCHAIR.get() && (!chair.hasInventory() || chair.getInventory().isEmpty()))
					chair.convertToItem((PlayerEntity)living);
			}
		});
		
		// Clear binding to any other walker when a player binds to a walker
		ServerEvents.ON_ENTITY_PARENT.register((living, walker) -> IParentedEntity.clearParentedEntities(living, walker));
	}
}
