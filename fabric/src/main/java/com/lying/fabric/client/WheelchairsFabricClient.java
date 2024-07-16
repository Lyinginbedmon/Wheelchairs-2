package com.lying.fabric.client;

import com.lying.client.WheelchairsClient;
import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.EntityStoolRenderer;
import com.lying.client.renderer.entity.EntityWalkerRenderer;
import com.lying.client.renderer.entity.EntityWheelchairRenderer;
import com.lying.init.WHCEntityTypes;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;

public final class WheelchairsFabricClient implements ClientModInitializer
{
	public void onInitializeClient()
	{
		WheelchairsClient.clientInit();
		WHCModelParts.init();
		
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
		EntityRendererRegistry.register(WHCEntityTypes.WALKER, EntityWalkerRenderer::new);
		EntityRendererRegistry.register(WHCEntityTypes.STOOL, EntityStoolRenderer::new);
	}
}
