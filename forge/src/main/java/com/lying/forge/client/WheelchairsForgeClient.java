package com.lying.forge.client;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.client.renderer.entity.EntityStoolRenderer;
import com.lying.client.renderer.entity.EntityWalkerRenderer;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WheelchairsForgeClient
{
    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info("Client init");
    	WheelchairsClient.clientInit();
    }
    
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		Wheelchairs.LOGGER.info("Entity renderer registration");
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchairRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.WALKER.get(), EntityWalkerRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.STOOL.get(), EntityStoolRenderer::new);
	}
}
