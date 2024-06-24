package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class ParentedEntityMovePacket
{
	public static void send(PlayerEntity entity)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeDouble(entity.getX());
		buffer.writeDouble(entity.getY());
		buffer.writeDouble(entity.getZ());
		buffer.writeFloat(entity.getYaw());
		buffer.writeFloat(entity.getPitch());
		ClientPlayNetworking.send(WHCPacketHandler.PARENTED_MOVE_ID, buffer);
	}
}
