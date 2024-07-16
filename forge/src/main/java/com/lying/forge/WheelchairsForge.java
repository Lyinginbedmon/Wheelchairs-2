package com.lying.forge;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsForge
{
	public WheelchairsForge()
	{
		EventBuses.registerModEventBus(Reference.ModInfo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		Wheelchairs.LOGGER.info("Common init");
		Wheelchairs.commonInit();
		
		final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::registerEntityAttributes);
	}
	
	public void registerEntityAttributes(final EntityAttributeCreationEvent event)
	{
		Wheelchairs.LOGGER.info("Entity attribute creation");
		event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createMountAttributes().build());
		event.put(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes().build());
		event.put(WHCEntityTypes.STOOL.get(), EntityStool.createMountAttributes().build());
	}
}
