package com.lying.forge.client;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.client.renderer.entity.EntityStoolRenderer;
import com.lying.client.renderer.entity.EntityWalkerRenderer;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WheelchairsForgeClient
{
    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info("Client init");
    	WheelchairsClient.clientInit();
    	
		final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::appendVestsEvent);
    }
    
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		Wheelchairs.LOGGER.info("Entity renderer registration");
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchairRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.WALKER.get(), EntityWalkerRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.STOOL.get(), EntityStoolRenderer::new);
	}
	
	public void appendVestsEvent(final AddLayers event)
	{
		// FIXME Resolve vest feature appending for Forge
//		Context context = event.getContext();
//		for(EntityType<? extends LivingEntity> type : ItemVest.APPLICABLE_MOBS.keySet())
//		{
//			LivingEntityRenderer<?, ? extends EntityModel<?>> renderer = event.getRenderer(type);
//			WheelchairsClient.appendVestFeature(type, renderer, context, (feature) -> renderer.addFeature(feature));
//		}
	}
}
