package com.lying.wheelchairs.network;

import com.lying.wheelchairs.entity.IFlyingMount;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class StartFlyingReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		if(player.hasVehicle() && player.getVehicle() instanceof IFlyingMount)
			server.execute(() -> 
			{
				((IFlyingMount)player.getVehicle()).startFlying();
			});
	}
}
