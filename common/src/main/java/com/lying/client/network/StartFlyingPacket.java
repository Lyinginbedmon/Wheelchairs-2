package com.lying.client.network;

import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class StartFlyingPacket
{
	public static void send()
	{
		NetworkManager.sendToServer(WHCPacketHandler.FLYING_START_ID, new PacketByteBuf(Unpooled.buffer()));
	}
}
