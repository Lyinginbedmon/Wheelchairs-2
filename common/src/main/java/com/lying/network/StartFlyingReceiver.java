package com.lying.network;

import com.lying.entity.IFlyingMount;

import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class StartFlyingReceiver implements NetworkReceiver
{
	public void receive(PacketByteBuf buf, PacketContext context)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
		if(player.hasVehicle() && player.getVehicle() instanceof IFlyingMount)
			context.queue(() -> ((IFlyingMount)player.getVehicle()).startFlying());
	}
}
