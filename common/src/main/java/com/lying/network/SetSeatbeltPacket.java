package com.lying.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class SetSeatbeltPacket
{
	private static final Identifier PACKET_ID = WHCPacketHandler.SET_SEATBELT_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void send(boolean setting)
	{
		NetworkManager.sendToServer(new Payload(setting));
	}
	
	public static record Payload(boolean setting) implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this(buffer.readBoolean());
		}
		
		public void write(RegistryByteBuf buffer)
		{
			buffer.writeBoolean(setting);
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
