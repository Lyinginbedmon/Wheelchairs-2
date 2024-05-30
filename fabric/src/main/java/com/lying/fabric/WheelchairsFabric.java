package com.lying.fabric;

import com.lying.Wheelchairs;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.network.FlyingMountRocketReceiver;
import com.lying.network.OpenInventoryScreenReceiver;
import com.lying.network.StartFlyingReceiver;
import com.lying.network.WHCPacketHandler;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class WheelchairsFabric implements ModInitializer
{
    public void onInitialize()
    {
    	Wheelchairs.commonInit();
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createChairAttributes());
    	
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.OPEN_INVENTORY_ID, new OpenInventoryScreenReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_START_ID, new StartFlyingReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_ROCKET_ID, new FlyingMountRocketReceiver());
    }
}
