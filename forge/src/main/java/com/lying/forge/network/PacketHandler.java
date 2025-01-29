package com.lying.forge.network;

import com.lying.reference.Reference;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler
{
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new Identifier(Reference.ModInfo.MOD_ID, "chan"), () -> "1.0", s -> true, s-> true);
	
	private PacketHandler(){ }
	
	public static void init()
	{
		int id = 0;
		HANDLER.registerMessage(id++, PacketSyncVest.class, PacketSyncVest::encode, PacketSyncVest::decode, PacketSyncVest::handle);
	}
	
	public static void sendTo(ServerPlayerEntity playerMP, Object toSend)
	{
		HANDLER.sendTo(toSend, playerMP.networkHandler.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
