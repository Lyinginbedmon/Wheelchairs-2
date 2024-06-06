package com.lying.neoforge;

import com.lying.Wheelchairs;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.network.FlyingMountRocketReceiver;
import com.lying.network.OpenInventoryScreenReceiver;
import com.lying.network.StartFlyingReceiver;
import com.lying.network.WHCPacketHandler;
import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.hooks.forge.EventBusesHooksImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsNeoForge
{
    public WheelchairsNeoForge()
    {
    	Wheelchairs.commonInit();
    	IEventBus eventBus = EventBusesHooksImpl.getModEventBus(Reference.ModInfo.MOD_ID).get();
        eventBus.addListener(this::registerEntityAttributes);
        
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.OPEN_INVENTORY_ID, new OpenInventoryScreenReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_START_ID, new StartFlyingReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_ROCKET_ID, new FlyingMountRocketReceiver());
    }
    
    public void registerEntityAttributes(final EntityAttributeCreationEvent event)
    {
    	event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createChairAttributes().build());
    }
}
