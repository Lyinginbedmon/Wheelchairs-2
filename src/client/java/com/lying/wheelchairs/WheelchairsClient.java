package com.lying.wheelchairs;

import org.lwjgl.glfw.GLFW;

import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItemsClient;
import com.lying.wheelchairs.init.WHCKeybinds;
import com.lying.wheelchairs.init.WHCScreenHandlerTypes;
import com.lying.wheelchairs.network.OpenInventoryScreenPacket;
import com.lying.wheelchairs.renderer.entity.EntityWheelchairRenderer;
import com.lying.wheelchairs.screen.ChairInventoryScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.util.InputUtil;

public class WheelchairsClient implements ClientModInitializer
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public void onInitializeClient()
	{
		WHCItemsClient.registerItemColors();
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
		WHCKeybinds.keyOpenChair = WHCKeybinds.make("open_chair", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C);
		
		HandledScreens.register(WHCScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, ChairInventoryScreen::new);
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> 
		{
			while(WHCKeybinds.keyOpenChair.wasPressed() && mc.player != null && mc.player.hasVehicle())
				OpenInventoryScreenPacket.send();
		});
	}
}