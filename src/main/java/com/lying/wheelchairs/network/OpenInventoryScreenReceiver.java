package com.lying.wheelchairs.network;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.screen.ChairInventoryScreenHandler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenInventoryScreenReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && ((EntityWheelchair)player.getVehicle()).hasInventory())
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ChairInventoryScreenHandler(id, playerInventory, ((EntityWheelchair)custom.getVehicle()).getInventory()), player.getVehicle().getDisplayName()));
	}
}
