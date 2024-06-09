package com.lying.neoforge;

import com.lying.Wheelchairs;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

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
    }
    
    public void registerEntityAttributes(final EntityAttributeCreationEvent event)
    {
    	event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createChairAttributes().build());
    }
}
