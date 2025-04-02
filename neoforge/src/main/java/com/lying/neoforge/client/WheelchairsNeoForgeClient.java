package com.lying.neoforge.client;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.WheelchairEntityRenderer;
import com.lying.client.screen.ChairInventoryScreen;
import com.lying.client.screen.WalkerInventoryScreen;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCScreenHandlerTypes;
import com.lying.neoforge.WheelchairsNeoForge;
import com.lying.neoforge.network.SyncVestPacket;
import com.lying.reference.Reference;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.CatEntityRenderer;
import net.minecraft.client.render.entity.FoxEntityRenderer;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WheelchairsNeoForgeClient
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
		Wheelchairs.LOGGER.info(" # [CLIENT] Init");
    	WheelchairsClient.clientInit();
    	WheelchairsNeoForge.getLocalPlayer = () -> MinecraftClient.getInstance().player;
    	
		NetworkManager.registerReceiver(NetworkManager.s2c(), SyncVestPacket.PACKET_TYPE, SyncVestPacket.PACKET_CODEC, (value, context) -> 
		{
			PlayerEntity localPlayer = WheelchairsNeoForge.getLocalPlayer.get();
			if(localPlayer == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before player entity created #");
				return;
			}
			
			World world = localPlayer.getWorld();
			if(world == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before world set #");
				return;
			}
			else
				world.getEntitiesByClass(LivingEntity.class, localPlayer.getBoundingBox().expand(5000D), e -> e.getUuid().equals(value.entityID())).forEach(e -> Wheelchairs.HANDLER.setVest(e, value.vestStack()));
		});
    }
    
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event)
    {
    	event.register(WHCScreenHandlerTypes.WHEELCHAIR_INVENTORY_HANDLER.get(), ChairInventoryScreen::new);
    	event.register(WHCScreenHandlerTypes.WALKER_INVENTORY_HANDLER.get(), WalkerInventoryScreen::new);
    }
    
    @SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		Wheelchairs.LOGGER.info(" # [CLIENT] Registering entity renderers");
		event.registerEntityRenderer(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntityRenderer::new);
//		event.registerEntityRenderer(WHCEntityTypes.WALKER, WalkerEntityRenderer::new);
//		event.registerEntityRenderer(WHCEntityTypes.STOOL, StoolEntityRenderer::new);
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
		((WolfEntityRenderer)event.getRenderer(EntityType.WOLF)).addFeature(WheelchairsClient.getVestFeatureForType(EntityType.WOLF, event.getRenderer(EntityType.WOLF)));
		((CatEntityRenderer)event.getRenderer(EntityType.CAT)).addFeature(WheelchairsClient.getVestFeatureForType(EntityType.CAT, event.getRenderer(EntityType.CAT)));
		((ParrotEntityRenderer)event.getRenderer(EntityType.PARROT)).addFeature(WheelchairsClient.getVestFeatureForType(EntityType.PARROT, event.getRenderer(EntityType.PARROT)));
		((FoxEntityRenderer)event.getRenderer(EntityType.FOX)).addFeature(WheelchairsClient.getVestFeatureForType(EntityType.FOX, event.getRenderer(EntityType.FOX)));
	}
}
