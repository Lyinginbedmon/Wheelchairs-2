package com.lying.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class AACMessageReceiver implements NetworkReceiver
{
	public void receive(PacketByteBuf buf, PacketContext context)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeText(buf.readText());
		buffer.writeUuid(player.getUuid());
		buffer.writeText(player.getDisplayName());
		player.getServer().getPlayerManager().getPlayerList().forEach(p -> NetworkManager.sendToPlayer(p, WHCPacketHandler.AAC_MESSAGE_RECEIVE_ID, buffer));
	}
}
