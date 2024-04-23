package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class OpenInventoryScreenPacket
{
	public static void send()
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientPlayNetworking.send(WHCPacketHandler.OPEN_INVENTORY_ID, buffer);
	}
}
