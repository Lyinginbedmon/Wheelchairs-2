package com.lying.wheelchairs.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.lying.wheelchairs.init.WHCEntityTypes;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public class ServerBus
{
	/**
	 * Fired BEFORE the player changes dimension, such as through using a portal
	 */
	public static final Event<BeforePlayerChange> BEFORE_PLAYER_CHANGE_WORLD = EventFactory.createArrayBacked(BeforePlayerChange.class, callbacks -> (originalEntity, origin, destination) -> 
	{
		for(BeforePlayerChange callback : callbacks)
			callback.beforeChangeWorld(originalEntity, origin, destination);
	});
	
	@FunctionalInterface
	public interface BeforePlayerChange
	{
		void beforeChangeWorld(PlayerEntity player, ServerWorld origin, ServerWorld destination);
	}
	
	/** Map of player UUIDs to the NBT data of the wheelchair they changed dimension whilst riding, if any */
	private static final Map<UUID, NbtCompound> DIMENSIONAL_WHEELCHAIRS = new HashMap<>();
	
	public static void registerEventCallbacks()
	{
		BEFORE_PLAYER_CHANGE_WORLD.register((original, origin, dest) -> storeEntityWheelchairIfNeeded(original));
		
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> loadEntityWheelchairIfNeeded(player, player, destination));
	}
	
	private static void storeEntityWheelchairIfNeeded(Entity original)
	{
		/**
		 * When entity changes dimension,
		 * IF it was riding a wheelchair
		 * THEN store wheelchair in memory
		 * THEN, after entity finishes moving, teleport wheelchair to entity & mount up
		 */
		
		if(!original.hasVehicle() || original.getVehicle().getType() != WHCEntityTypes.WHEELCHAIR)
			return;
		
		Entity wheelchair = original.getVehicle();
		NbtCompound chairData = new NbtCompound();
		wheelchair.saveNbt(chairData);
		DIMENSIONAL_WHEELCHAIRS.put(original.getUuid(), chairData);
		original.dismountVehicle();
		wheelchair.discard();
		
		System.out.println("Stored wheelchair ridden by "+original.getName().getString());
	}
	
	private static void loadEntityWheelchairIfNeeded(Entity originalEntity, Entity newEntity, ServerWorld destination)
	{
		UUID uuid = originalEntity.getUuid();
		if(!DIMENSIONAL_WHEELCHAIRS.containsKey(uuid))
			return;
		
		NbtCompound chairData = DIMENSIONAL_WHEELCHAIRS.remove(uuid);
		Optional<Entity> wheelchairOpt = EntityType.getEntityFromNbt(chairData, destination);
		if(wheelchairOpt.isEmpty())
			return;
		
		Entity wheelchair = wheelchairOpt.get();
		wheelchair.copyPositionAndRotation(newEntity);
		destination.spawnEntity(wheelchair);
		newEntity.startRiding(wheelchair);
		
		System.out.println("Reloaded wheelchair ridden by "+newEntity.getName().getString());
	}
}
