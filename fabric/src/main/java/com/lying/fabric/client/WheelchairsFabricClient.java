package com.lying.fabric.client;

import com.lying.client.WheelchairsClient;
import com.lying.client.init.WHCItemsClient;
import com.lying.client.init.WHCKeybinds;
import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.EntityStoolRenderer;
import com.lying.client.renderer.entity.EntityWalkerRenderer;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.CatEntityRenderer;
import net.minecraft.client.render.entity.FoxEntityRenderer;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.EntityType;

public final class WheelchairsFabricClient implements ClientModInitializer
{
	public void onInitializeClient()
	{
		WheelchairsClient.clientInit();
		WHCKeybinds.init(KeyMappingRegistry::register);
		WHCItemsClient.registerItemColors(ColorHandlerRegistry::registerItemColors);
		WHCModelParts.init((layer, func) -> EntityModelLayerRegistry.register(layer, func));
		
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
		EntityRendererRegistry.register(WHCEntityTypes.WALKER, EntityWalkerRenderer::new);
		EntityRendererRegistry.register(WHCEntityTypes.STOOL, EntityStoolRenderer::new);
		
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
				(entityType, entityRenderer, registrationHelper, context) -> 
				{
					if(entityType == EntityType.WOLF)
						registrationHelper.register(WheelchairsClient.getVestFeatureForType(EntityType.WOLF, (WolfEntityRenderer)entityRenderer));
					else if(entityType == EntityType.CAT)
						registrationHelper.register(WheelchairsClient.getVestFeatureForType(EntityType.CAT, (CatEntityRenderer)entityRenderer));
					else if(entityType == EntityType.PARROT)
						registrationHelper.register(WheelchairsClient.getVestFeatureForType(EntityType.PARROT, (ParrotEntityRenderer)entityRenderer));
					else if(entityType == EntityType.FOX)
						registrationHelper.register(WheelchairsClient.getVestFeatureForType(EntityType.FOX, (FoxEntityRenderer)entityRenderer));
				});
	}
}
