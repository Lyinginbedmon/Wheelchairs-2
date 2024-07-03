package com.lying.wheelchairs.network;

import java.util.UUID;

import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.screen.ChairInventoryScreenHandler;
import com.lying.wheelchairs.screen.WalkerInventoryScreenHandler;

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
		// If in wheelchair, open wheelchair inventory
		if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && ((EntityWheelchair)player.getVehicle()).hasInventory())
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ChairInventoryScreenHandler(id, playerInventory, ((EntityWheelchair)custom.getVehicle())), player.getVehicle().getDisplayName()));
		// Else if player was looking at an entity, try to open its inventory
		else if(buf.readBoolean())
		{
			UUID uuid = buf.readUuid();
			player.getWorld().getEntitiesByType(WHCEntityTypes.WALKER, player.getBoundingBox().expand(4D), EntityWalker::hasInventory).forEach(walker -> 
			{
				if(walker.getUuid().equals(uuid))
				{
					if(walker.hasInventory())
						player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new WalkerInventoryScreenHandler(id, playerInventory, walker.getInventory(), walker), walker.getDisplayName()));
					else
						return;
				}
			});
		}
	}
}
