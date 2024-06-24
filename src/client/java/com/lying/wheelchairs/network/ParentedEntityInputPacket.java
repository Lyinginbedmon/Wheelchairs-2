package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class ParentedEntityInputPacket
{
	public static void send(float sideways, float forward, boolean jump, boolean sneak)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeFloat(sideways);
		buffer.writeFloat(forward);
		buffer.writeBoolean(jump);
		buffer.writeBoolean(sneak);
		ClientPlayNetworking.send(WHCPacketHandler.PARENTED_INPUT_ID, buffer);
	}
}
