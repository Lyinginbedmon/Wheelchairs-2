package com.lying.wheelchairs.network;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class ParentedEntityPositionReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		UUID entityID = buf.readUuid();
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		
		client.execute(() -> 
		{
			PlayerEntity player = client.player;
			if(player == null || player.getWorld() == null)
				return;
			
			player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(16D), ent -> ent.getUuid().equals(entityID)).forEach(ent -> ent.updatePosition(x, y, z));
		});
	}
}
