package com.lying.wheelchairs.network;

import java.util.UUID;

import com.google.common.base.Predicates;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ParentedEntityPositionPacket
{
	public static void send(UUID childID, Vec3d updatedPos, Entity entity)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(childID);
		buffer.writeDouble(updatedPos.getX());
		buffer.writeDouble(updatedPos.getY());
		buffer.writeDouble(updatedPos.getZ());
		
		entity.getWorld().getEntitiesByType(EntityType.PLAYER, entity.getBoundingBox().expand(16D), Predicates.alwaysTrue())
		.forEach(player -> ServerPlayNetworking.send((ServerPlayerEntity)player, WHCPacketHandler.PARENTED_MOVE_ID, buffer));
	}
}
