package com.lying.fabric.client;

import com.lying.client.WheelchairsClient;
import com.lying.client.renderer.entity.WheelchairEntityRenderer;
import com.lying.init.WHCEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class WheelchairsFabricClient implements ClientModInitializer
{
    public void onInitializeClient()
    {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    	WheelchairsClient.clientInit();
    	
    	EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntityRenderer::new);
    }
}
