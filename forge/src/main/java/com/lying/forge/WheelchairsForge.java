package com.lying.forge;

import com.lying.Wheelchairs;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.network.FlyingMountRocketReceiver;
import com.lying.network.OpenInventoryScreenReceiver;
import com.lying.network.StartFlyingReceiver;
import com.lying.network.WHCPacketHandler;
import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsForge
{
    public WheelchairsForge()
    {
        EventBuses.registerModEventBus(Reference.ModInfo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::registerEntityAttributes);
        
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.OPEN_INVENTORY_ID, new OpenInventoryScreenReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_START_ID, new StartFlyingReceiver());
    	NetworkManager.registerReceiver(NetworkManager.c2s(), WHCPacketHandler.FLYING_ROCKET_ID, new FlyingMountRocketReceiver());
    }
    
    public void registerEntityAttributes(EntityAttributeCreationEvent event)
    {
    	event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createChairAttributes().build());
    }
    
    public void setupCommon(final FMLCommonSetupEvent event)
    {
    	Wheelchairs.commonInit();
    }
}
