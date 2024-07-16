package com.lying.network;

import java.util.UUID;

import com.lying.entity.ChairUpgrade;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.screen.ChairInventoryScreenHandler;
import com.lying.screen.WalkerInventoryScreenHandler;

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
		// If in wheelchair, open wheelchair inventory
		if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get() && ((EntityWheelchair)player.getVehicle()).getUpgrades().stream().anyMatch(ChairUpgrade::enablesScreen))
		{
			EntityWheelchair vehicle = (EntityWheelchair)player.getVehicle();
			MenuRegistry.openMenu(player, new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ChairInventoryScreenHandler(id, playerInventory, vehicle), vehicle.getDisplayName()));
			return;
		}
		
		// Else if player was looking at an entity, try to open its inventory
		if(buf.readBoolean())
		{
			UUID uuid = buf.readUuid();
			player.getWorld().getEntitiesByType(WHCEntityTypes.WALKER.get(), player.getBoundingBox().expand(4D), EntityWalker::hasInventory).forEach(walker -> 
			{
				if(walker.getUuid().equals(uuid))
					MenuRegistry.openMenu(player, new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new WalkerInventoryScreenHandler(id, playerInventory, walker.getInventory(), walker), walker.getDisplayName()));
			});
		}
	}
}
