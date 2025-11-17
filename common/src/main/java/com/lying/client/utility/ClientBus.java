package com.lying.client.utility;

import java.util.Map;

import com.lying.client.WheelchairsClient;
import com.lying.client.event.RenderEvents;
import com.lying.client.renderer.entity.feature.CatVestLayer;
import com.lying.client.renderer.entity.feature.FoxVestLayer;
import com.lying.client.renderer.entity.feature.ParrotVestLayer;
import com.lying.client.renderer.entity.feature.WolfVestLayer;
import com.lying.entity.IFlyingMount;
import com.lying.mixin.AccessorEntityRenderDispatcher;
import com.lying.mixin.AccessorLivingEntityRenderer;
import com.lying.network.SetSeatbeltPacket;
import com.lying.network.StartFlyingPacket;
import com.lying.utility.ServerEvents;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.render.entity.CatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.FoxEntityRenderer;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.EntityType;

public class ClientBus
{
	public static void registerEventCallbacks()
	{
		ServerEvents.ON_DOUBLE_JUMP.register((living) -> 
		{
			if(living instanceof IFlyingMount && ((IFlyingMount)living).canStartFlying())
				StartFlyingPacket.send();
		});
		
		RenderEvents.ADD_FEATURE_RENDERERS_EVENT.register(dispatcher -> 
		{
			AccessorEntityRenderDispatcher accessor = (AccessorEntityRenderDispatcher)dispatcher;
			Map<EntityType<?>, EntityRenderer<?,?>> renderers = accessor.getRenderers();
			
			WolfEntityRenderer wolfRenderer = (WolfEntityRenderer)renderers.get(EntityType.WOLF);
			((AccessorLivingEntityRenderer)wolfRenderer).appendFeature(new WolfVestLayer(wolfRenderer));
			CatEntityRenderer catRenderer = (CatEntityRenderer)renderers.get(EntityType.CAT);
			((AccessorLivingEntityRenderer)catRenderer).appendFeature(new CatVestLayer(catRenderer));
			ParrotEntityRenderer parrotRenderer = (ParrotEntityRenderer)renderers.get(EntityType.PARROT);
			((AccessorLivingEntityRenderer)parrotRenderer).appendFeature(new ParrotVestLayer(parrotRenderer));
			FoxEntityRenderer foxRenderer = (FoxEntityRenderer)renderers.get(EntityType.FOX);
			((AccessorLivingEntityRenderer)foxRenderer).appendFeature(new FoxVestLayer(foxRenderer));
		});
		
		// Ping server on join with initial seatbelt setting
		PlayerEvent.PLAYER_JOIN.register(p -> SetSeatbeltPacket.send(WheelchairsClient.SEATBELT_ON));
	}
}
