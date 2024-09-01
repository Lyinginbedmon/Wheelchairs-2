package com.lying.client.network;

import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;

public class AACMessagePacket
{
	public static void send(MutableText message)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeText(message.copy().styled(style -> style.withClickEvent(null)));
		NetworkManager.sendToServer(WHCPacketHandler.AAC_MESSAGE_SEND_ID, buffer);
	}
}