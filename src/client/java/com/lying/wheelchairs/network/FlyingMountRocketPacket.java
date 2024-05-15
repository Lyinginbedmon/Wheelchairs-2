package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public class FlyingMountRocketPacket
{
	public static void send(Hand hand)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(hand);
		ClientPlayNetworking.send(WHCPacketHandler.FLYING_ROCKET_ID, buffer);
	}
}
