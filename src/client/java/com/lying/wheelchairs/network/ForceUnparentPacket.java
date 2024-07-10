package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class ForceUnparentPacket
{
	public static void send()
	{
		ClientPlayNetworking.send(WHCPacketHandler.FORCE_UNPARENT_ID, new PacketByteBuf(Unpooled.buffer()));
	}
}
