package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class StartFlyingPacket
{
	public static void send()
	{
		ClientPlayNetworking.send(WHCPacketHandler.FLYING_START_ID, new PacketByteBuf(Unpooled.buffer()));
	}
}
