package com.lying.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ForceUnparentPacket
{
	private static final Identifier PACKET_ID = WHCPacketHandler.FORCE_UNPARENT_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void send()
	{
		NetworkManager.sendToServer(new Payload());
	}
	
	public static record Payload() implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this();
		}
		
		public void write(RegistryByteBuf buffer) { }
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
