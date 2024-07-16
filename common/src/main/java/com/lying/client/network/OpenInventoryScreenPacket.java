package com.lying.client.network;

import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
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
		
		NetworkManager.sendToServer(WHCPacketHandler.OPEN_INVENTORY_ID, buffer);
	}
}
