package com.lying.wheelchairs;

import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItemsClient;
import com.lying.wheelchairs.renderer.entity.EntityWheelchairRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class WheelchairsClient implements ClientModInitializer
{
	public void onInitializeClient()
	{
		WHCItemsClient.registerItemColors();
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
	}
}