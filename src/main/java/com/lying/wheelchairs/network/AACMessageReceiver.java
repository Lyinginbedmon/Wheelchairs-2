package com.lying.wheelchairs.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class AACMessageReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		buf.writeUuid(player.getUuid());
		buf.writeText(player.getDisplayName());
		server.getPlayerManager().getPlayerList().forEach(p -> ServerPlayNetworking.send(p, WHCPacketHandler.AAC_MESSAGE_RECEIVE_ID, buf));
	}
}
