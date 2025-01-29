package com.lying.forge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import com.lying.forge.capability.VestCapability;
import com.lying.forge.network.PacketHandler;
import com.lying.forge.network.PacketSyncVest;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
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
		if(event.isCanceled() || event.getEntity().getWorld().isClient()) return;
		syncServiceAnimalsToPlayer(event.getEntity(), event.getTo());
	}
	
	private static void syncServiceAnimalsToPlayer(PlayerEntity player, RegistryKey<World> newDim)
	{
		SERVICE_ANIMALS.getOrDefault(newDim, Lists.newArrayList()).forEach(ent -> 
		{
			VestCapability cap = ent.getCapability(WheelchairsForge.VEST_DATA).orElse(null);
			if(cap == null) return;
			syncServiceAnimalToPlayer(ent, player);
		});
	}
	
	public static void syncServiceAnimalToPlayers(Entity ent)
	{
		VestCapability cap = ent.getCapability(WheelchairsForge.VEST_DATA).orElse(null);
		if(cap == null) return;
		
		World world = ent.getWorld();
		world.getPlayers().forEach(player -> syncServiceAnimalToPlayer(ent, player));
	}
	
	private static void syncServiceAnimalToPlayer(Entity ent, PlayerEntity player)
	{
		VestCapability cap = ent.getCapability(WheelchairsForge.VEST_DATA).orElse(null);
		if(cap == null)
			return;
		
		World world = ent.getWorld();
		if(world.isClient() || player.getWorld().getRegistryKey() != world.getRegistryKey())
			return;
		
		PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncVest(ent.getUuid(), cap.get()));
	}
}
