package com.lying.network;

import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;

import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ForceUnparentReceiver implements NetworkReceiver
{
	public void receive(PacketByteBuf buf, PacketContext context)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
		player.getServer().execute(() -> 
		{
			if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && ((EntityWheelchair)player.getVehicle()).hasParent())
				((EntityWheelchair)player.getVehicle()).forceUnbind();
		});
	}
}
