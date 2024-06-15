package com.lying.client.network;

import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class OpenInventoryScreenPacket
{
	public static void send()
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		NetworkManager.sendToServer(WHCPacketHandler.OPEN_INVENTORY_ID, buffer);
	}
}
