package com.lying.network;

import java.util.UUID;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class OpenInventoryScreenPacket
{
	private static final Identifier PACKET_ID = WHCPacketHandler.OPEN_INVENTORY_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void send()
	{
		send(null);
	}
	
	public static void send(UUID targetID)
	{
		NetworkManager.sendToServer(new Payload(targetID != null, targetID));
	}
	
	public static record Payload(boolean openTarget, UUID entityID) implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this(buffer.readBoolean(), buffer.readUuid());
		}
		
		public void write(RegistryByteBuf buffer)
		{
			buffer.writeBoolean(openTarget);
			buffer.writeUuid(entityID == null ? UUID.randomUUID() : entityID);
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
