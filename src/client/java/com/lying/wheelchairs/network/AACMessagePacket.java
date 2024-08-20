package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;

public class AACMessagePacket
{
	public static void send(MutableText message)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeText(message.copy().styled(style -> style.withClickEvent(null)));
		ClientPlayNetworking.send(WHCPacketHandler.AAC_MESSAGE_SEND_ID, buffer);
	}
}