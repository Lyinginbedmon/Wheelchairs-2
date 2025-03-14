package com.lying.network;

import java.util.UUID;

import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public class AACMessagePacket
{
	private static final Identifier PACKET_ID = WHCPacketHandler.AAC_MESSAGE_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void sendToServer(Text message, PlayerEntity player)
	{
		NetworkManager.sendToServer(new Payload(message, player.getUuid(), player.getDisplayName()));
	}
	
	public static void sendToPlayer(ServerPlayerEntity player, Payload payload)
	{
		NetworkManager.sendToPlayer(player, payload);
	}
	
	public static record Payload(Text message, UUID playerID, Text playerName) implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this(null, buffer.readUuid(), null);
		}
		
		public void write(RegistryByteBuf buffer)
		{
			TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buffer, message);
			buffer.writeUuid(playerID);
			TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buffer, playerName);
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
