package com.lying.network;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class OpenInventoryScreenPacket
{
	private static final Identifier PACKET_ID = WHCPacketHandler.OPEN_INVENTORY_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::read);
	
	public static void send()
	{
		send(null);
	}
	
	public static void send(UUID targetID)
	{
		NetworkManager.sendToServer(new Payload(targetID == null ? Optional.empty() : Optional.of(targetID)));
	}
	
	public static class Payload implements CustomPayload
	{
		private Optional<UUID> targetId = Optional.empty();
		
		protected Payload(@NotNull Optional<UUID> idIn)
		{
			targetId = idIn;
		}
		
		public static Payload read(RegistryByteBuf buffer)
		{
			if(buffer.readBoolean())
				return new Payload(Optional.of(buffer.readUuid()));
			else
				return new Payload(Optional.empty());
		}
		
		public void write(RegistryByteBuf buffer)
		{
			targetId.ifPresentOrElse(
					id -> { buffer.writeBoolean(true); buffer.writeUuid(id); }, 
					() -> buffer.writeBoolean(false));
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
		
		public boolean openTarget() { return targetId.isPresent(); }
		
		public UUID entityID() { return targetId.get(); }
	}
}
