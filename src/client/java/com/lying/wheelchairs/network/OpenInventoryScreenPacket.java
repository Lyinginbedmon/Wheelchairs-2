package com.lying.wheelchairs.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class OpenInventoryScreenPacket
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public static void send()
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		
		Entity targetedEntity = mc.targetedEntity;
		buffer.writeBoolean(targetedEntity != null);
		if(targetedEntity != null)
			buffer.writeUuid(targetedEntity.getUuid());
		
		ClientPlayNetworking.send(WHCPacketHandler.OPEN_INVENTORY_ID, buffer);
	}
}
