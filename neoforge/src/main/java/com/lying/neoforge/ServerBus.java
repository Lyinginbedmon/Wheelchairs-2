package com.lying.neoforge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.item.ItemVest;
import com.lying.neoforge.capability.VestCapability;
import com.lying.neoforge.network.SyncVestPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

public class ServerBus
{
	private static Map<RegistryKey<World>,List<Entity>> SERVICE_ANIMALS = new HashMap<>();
	
	@SubscribeEvent
	public static void serviceAnimalLoaded(final EntityJoinLevelEvent event)
	{
		if(ItemVest.isMobWithVest(event.getEntity()))
			startTrackingVest(event.getEntity());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void serviceAnimalUnloaded(final EntityLeaveLevelEvent event)
	{
		if(ItemVest.isMobWithVest(event.getEntity()))
			stopTrackingVest(event.getEntity());
	}
	
	@SubscribeEvent
	public static void serviceAnimalVestChanged(final VestChangeEvent event)
	{
		if(event.isEmpty())
			stopTrackingVest(event.getEntity());
		else
			startTrackingVest(event.getEntity());
		
		syncServiceAnimalToPlayers((LivingEntity)event.getEntity());
	}
	
	private static void stopTrackingVest(Entity ent)
	{
		if(ent.getWorld().isClient()) return;
		
		RegistryKey<World> world = ent.getWorld().getRegistryKey();
		List<Entity> entities = SERVICE_ANIMALS.getOrDefault(world, Lists.newArrayList());
		entities.removeIf(e -> e.getUuid().equals(ent.getUuid()));
		SERVICE_ANIMALS.put(world, entities);
	}
	
	private static void startTrackingVest(Entity ent)
	{
		if(ent.getWorld().isClient()) return;
		stopTrackingVest(ent);
		
		RegistryKey<World> world = ent.getWorld().getRegistryKey();
		List<Entity> entities = SERVICE_ANIMALS.getOrDefault(world, Lists.newArrayList());
		entities.add(ent);
		SERVICE_ANIMALS.put(world, entities);
		syncServiceAnimalToPlayers(ent);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void syncVestsToPlayer(final PlayerLoggedInEvent event)
	{
		PlayerEntity player = event.getEntity();
		World world = player.getWorld();
		if(world.isClient()) return;
		
		syncServiceAnimalsToPlayer(player, player.getWorld().getRegistryKey());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void syncVestsFromPortal(final PlayerChangedDimensionEvent event)
	{
		if(event.getEntity().getWorld().isClient()) return;
		syncServiceAnimalsToPlayer(event.getEntity(), event.getTo());
	}
	
	private static void syncServiceAnimalsToPlayer(PlayerEntity player, RegistryKey<World> newDim)
	{
		SERVICE_ANIMALS.getOrDefault(newDim, Lists.newArrayList()).forEach(ent -> 
		{
			VestCapability cap = ent.getCapability(WheelchairsNeoForge.VEST_DATA);
			if(cap == null) return;
			syncServiceAnimalToPlayer(ent, player);
		});
	}
	
	public static void syncServiceAnimalToPlayers(Entity ent)
	{
		VestCapability cap = ent.getCapability(WheelchairsNeoForge.VEST_DATA);
		if(cap == null) return;
		
		World world = ent.getWorld();
		world.getPlayers().forEach(player -> syncServiceAnimalToPlayer(ent, player));
	}
	
	private static void syncServiceAnimalToPlayer(Entity ent, PlayerEntity player)
	{
		VestCapability cap = ent.getCapability(WheelchairsNeoForge.VEST_DATA);
		if(cap == null)
			return;
		
		World world = ent.getWorld();
		if(world.isClient() || player.getWorld().getRegistryKey() != world.getRegistryKey())
			return;
		
		SyncVestPacket.sendTo((ServerPlayerEntity)player, ent.getUuid(), cap.get());
	}
}
