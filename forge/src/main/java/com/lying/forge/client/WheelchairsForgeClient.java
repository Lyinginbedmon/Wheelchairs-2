package com.lying.forge.client;

import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.client.init.WHCItemsClient;
import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.EntityStoolRenderer;
import com.lying.client.renderer.entity.EntityWalkerRenderer;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WheelchairsForgeClient
{
	// FIXME Ensure that keybindings are registered and functional on Forge
	
    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info("Client init");
    	WheelchairsClient.clientInit();
    	
		final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::appendVestsEvent);
    }
    
    @SuppressWarnings("deprecation")
    @SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
    	WHCItemsClient.registerItemColors((provider, items) -> 
    	{
    		for(Supplier<? extends Item> supplier : items)
    			event.getItemColors().register(provider, supplier.get());
    	});
    }
    
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		Wheelchairs.LOGGER.info("Entity renderer registration");
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchairRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.WALKER.get(), EntityWalkerRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.STOOL.get(), EntityStoolRenderer::new);
	}
	
	@SubscribeEvent
	public static void registerModelParts(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		WHCModelParts.init((layer, func) -> event.registerLayerDefinition(layer, func));
	}
	
	public void appendVestsEvent(final AddLayers event)
	{
		// FIXME Resolve vest feature appending for Forge
		Context context = event.getContext();
		for(EntityType<? extends LivingEntity> type : ItemVest.APPLICABLE_MOBS.keySet())
		{
			LivingEntityRenderer<?, ? extends EntityModel<?>> renderer = event.getRenderer(type);
//			WheelchairsClient.appendVestFeature(type, renderer, context, (feature) -> renderer.addFeature(feature));
		}
	}
}
