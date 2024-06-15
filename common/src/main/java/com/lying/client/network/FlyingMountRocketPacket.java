package com.lying.client.network;

import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public class FlyingMountRocketPacket
{
	public static void send(Hand hand)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(hand);
		NetworkManager.sendToServer(WHCPacketHandler.FLYING_ROCKET_ID, buffer);
	}
}
