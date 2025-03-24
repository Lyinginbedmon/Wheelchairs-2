package com.lying.network;

import java.util.UUID;

import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public class AACMessagePacket
{
	public static void sendToServer(Text message, PlayerEntity player)
	{
		NetworkManager.sendToServer(new Payload.Send(message, player.getUuid(), player.getDisplayName()));
	}
	
	public static void sendToPlayer(ServerPlayerEntity player, Payload.Send payload)
	{
		NetworkManager.sendToPlayer(player, payload.echo());
	}
	
	public static abstract class Payload implements CustomPayload
	{
		protected final Text message, playerName;
		protected final UUID playerID;
		
		protected Payload(Text message, UUID playerID, Text playerName)
		{
			this.message = message;
			this.playerID = playerID;
			this.playerName = playerName;
		}
		
		public Text message() { return message; }
		public Text playerName() { return playerName; }
		public UUID playerID() { return playerID; }
		
		protected static Identifier of(Side side)
		{
			return Reference.ModInfo.prefix("aac_message_"+side.name().toLowerCase());
		}
		
		/** Message sent from CLIENT to SERVER */
		public static class Send extends Payload
		{
			public static final Identifier PACKET_ID = of(Side.C2S);
			public static final CustomPayload.Id<Payload.Send> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
			public static final PacketCodec<RegistryByteBuf, Payload.Send> PACKET_CODEC	= PacketCodec.tuple(
					TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, Payload::message, 
					Uuids.PACKET_CODEC, Payload::playerID, 
					TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, Payload::playerName, Payload.Send::new);
			
			protected Send(Text message, UUID playerID, Text playerName) { super(message, playerID, playerName); }
			
			public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
			
			/** Returns a Receive equivalent of this message for use by the server */
			public Receive echo()
			{
				return new Payload.Receive(message, playerID, playerName);
			}
		}
		
		/** Message sent from SERVER to CLIENT */
		public static class Receive extends Payload
		{
			public static final Identifier PACKET_ID = of(Side.S2C);
			public static final CustomPayload.Id<Payload.Receive> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
			public static final PacketCodec<RegistryByteBuf, Payload.Receive> PACKET_CODEC	= PacketCodec.tuple(
					TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, Payload::message, 
					Uuids.PACKET_CODEC, Payload::playerID, 
					TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, Payload::playerName, Payload.Receive::new);
			
			protected Receive(Text message, UUID playerID, Text playerName) { super(message, playerID, playerName); }
			
			public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
		}
	}
}
