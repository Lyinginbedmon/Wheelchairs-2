package com.lying.network;

import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.screen.ChairInventoryScreenHandler;

import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenInventoryScreenReceiver implements NetworkReceiver
{
	public void receive(PacketByteBuf buf, PacketContext context)
	{
		ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
		if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get() && ((EntityWheelchair)player.getVehicle()).hasInventory())
			MenuRegistry.openMenu(player, new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ChairInventoryScreenHandler(id, playerInventory, ((EntityWheelchair)custom.getVehicle()).getInventory()), player.getVehicle().getDisplayName()));
	}
}
