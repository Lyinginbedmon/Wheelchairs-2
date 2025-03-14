package com.lying.neoforge.network;

import java.util.UUID;

import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SyncVestPacket
{
	private static final Identifier PACKET_ID = Reference.ModInfo.prefix("sync_vest");
	public static final CustomPayload.Id<Payload> PACKET_TYPE	= new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC	= CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void sendTo(ServerPlayerEntity player, UUID entityID, ItemStack stack)
	{
		NetworkManager.sendToPlayer(player, new Payload(entityID, stack));
	}
	
	public static record Payload(UUID entityID, ItemStack vestStack) implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this(buffer.readUuid(), ItemStack.PACKET_CODEC.decode(buffer));
		}
		
		public void write(RegistryByteBuf buffer)
		{
			buffer.writeUuid(entityID);
			ItemStack.PACKET_CODEC.encode(buffer, vestStack);
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
