package com.lying.forge.client;

import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.client.init.WHCItemsClient;
import com.lying.client.init.WHCKeybinds;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WheelchairsForgeClient
{
	// FIXME Ensure that keybindings are registered and functional on Forge
	
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info(" # [CLIENT] Init");
    	WheelchairsClient.clientInit();
    }
	
    @SubscribeEvent
	public static void registerKeyBindings(final RegisterKeyMappingsEvent event)
	{
		Wheelchairs.LOGGER.info(" # [CLIENT] Registering key bindings");
		WHCKeybinds.init(event::register);
	}
    
    @SubscribeEvent
    @SuppressWarnings("deprecation")
	public static void registerItemColors(final RegisterColorHandlersEvent.Item event)
    {
		Wheelchairs.LOGGER.info(" # [CLIENT] Registering item colors");
    	WHCItemsClient.registerItemColors((provider, items) -> 
    	{
    		for(Supplier<? extends Item> supplier : items)
    			event.getItemColors().register(provider, supplier.get());
    	});
    }
    
    @SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		Wheelchairs.LOGGER.info(" # [CLIENT] Registering entity renderers");
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchairRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.WALKER.get(), EntityWalkerRenderer::new);
		event.registerEntityRenderer(WHCEntityTypes.STOOL.get(), EntityStoolRenderer::new);
	}
	
    @SubscribeEvent
	public static void registerModelParts(final EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		Wheelchairs.LOGGER.info(" # [CLIENT] Registering entity model parts");
		WHCModelParts.init((layer, func) -> event.registerLayerDefinition(layer, func));
	}
	
    @SubscribeEvent
	public static void appendVestsEvent(final EntityRenderersEvent.AddLayers event)
	{
		Wheelchairs.LOGGER.info(" # [CLIENT] Appending entity service vest layers");
		// FIXME Resolve vest feature appending for Forge
		Context context = event.getContext();
		for(EntityType<? extends LivingEntity> type : ItemVest.APPLICABLE_MOBS.keySet())
		{
			LivingEntityRenderer<?, ? extends EntityModel<?>> renderer = event.getRenderer(type);
//			WheelchairsClient.appendVestFeature(type, renderer, context, (feature) -> renderer.addFeature(feature));
		}
	}
}
