package com.lying.neoforge.client;

import com.lying.client.WheelchairsClient;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WheelchairsForgeClient
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
    	WheelchairsClient.clientInit();
    }
    
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchairRenderer::new);
	}
}
