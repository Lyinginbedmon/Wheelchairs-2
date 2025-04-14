package com.lying.fabric.client;

import com.lying.client.WheelchairsClient;
import com.lying.client.renderer.entity.StoolEntityRenderer;
import com.lying.client.renderer.entity.WalkerEntityRenderer;
import com.lying.client.renderer.entity.WheelchairEntityRenderer;
import com.lying.client.screen.ChairInventoryScreen;
import com.lying.client.screen.WalkerInventoryScreen;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCScreenHandlerTypes;

import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class WheelchairsFabricClient implements ClientModInitializer
{
    public void onInitializeClient()
    {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    	WheelchairsClient.clientInit();
    	
		MenuRegistry.registerScreenFactory(WHCScreenHandlerTypes.WHEELCHAIR_INVENTORY_HANDLER.get(), ChairInventoryScreen::new);
		MenuRegistry.registerScreenFactory(WHCScreenHandlerTypes.WALKER_INVENTORY_HANDLER.get(), WalkerInventoryScreen::new);
    	
    	EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntityRenderer::new);
    	EntityRendererRegistry.register(WHCEntityTypes.WALKER.get(), WalkerEntityRenderer::new);
    	EntityRendererRegistry.register(WHCEntityTypes.STOOL.get(), StoolEntityRenderer::new);
    }
}
