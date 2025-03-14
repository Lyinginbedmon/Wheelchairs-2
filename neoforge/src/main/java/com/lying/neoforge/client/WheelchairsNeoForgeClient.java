package com.lying.neoforge.client;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.neoforge.WheelchairsNeoForge;
import com.lying.neoforge.network.SyncVestPacket;
import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WheelchairsNeoForgeClient
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info(" # [CLIENT] Init");
    	WheelchairsClient.clientInit();
    	WheelchairsNeoForge.getLocalPlayer = () -> MinecraftClient.getInstance().player;
		
		NetworkManager.registerReceiver(NetworkManager.s2c(), SyncVestPacket.PACKET_TYPE, SyncVestPacket.PACKET_CODEC, (value, context) -> 
		{
			PlayerEntity localPlayer = WheelchairsNeoForge.getLocalPlayer.get();
			if(localPlayer == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before player entity created #");
				return;
			}
			
			World world = localPlayer.getWorld();
			if(world == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before world set #");
				return;
			}
			else
				world.getEntitiesByClass(LivingEntity.class, localPlayer.getBoundingBox().expand(5000D), e -> e.getUuid().equals(value.entityID())).forEach(e -> Wheelchairs.HANDLER.setVest(e, value.vestStack()));
		});
    }
}
