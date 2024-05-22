package com.lying.wheelchairs;

import org.lwjgl.glfw.GLFW;

import com.lying.wheelchairs.config.ClientConfig;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItemsClient;
import com.lying.wheelchairs.init.WHCKeybinds;
import com.lying.wheelchairs.init.WHCScreenHandlerTypes;
import com.lying.wheelchairs.init.WHCSoundEvents;
import com.lying.wheelchairs.network.OpenInventoryScreenPacket;
import com.lying.wheelchairs.renderer.entity.EntityWheelchairRenderer;
import com.lying.wheelchairs.screen.ChairInventoryScreen;
import com.lying.wheelchairs.utility.ClientBus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class WheelchairsClient implements ClientModInitializer
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ClientConfig config;
	
	public static boolean SEATBELT_ON = false;
	private static boolean wasSeatbeltPressed = false;
	
	public void onInitializeClient()
	{
		config = new ClientConfig(mc.runDirectory.getAbsolutePath() + "/config/WheelchairsClient.cfg");
		config.read();
		
		ClientBus.registerEventCallbacks();
		WHCItemsClient.registerItemColors();
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
		WHCKeybinds.keyOpenChair = WHCKeybinds.make("open_chair", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C);
		WHCKeybinds.keySeatbelt = WHCKeybinds.make("seatbelt", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X);
		
		registerEventCallbacks();
		
		HandledScreens.register(WHCScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, ChairInventoryScreen::new);
	}
	
	public void registerEventCallbacks()
	{
		// Informs the player of their configured seatbelt setting when they log in
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> 
		{
			//FIXME Deactivate seatbelt if player logged in outside of a vehicle
			SEATBELT_ON = config.seatbeltAtBoot();
			client.player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(SEATBELT_ON ? "on" : "off")));
		});
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(player != null && player.hasVehicle())
			{
				// Allows for opening wheelchair inventory
				while(WHCKeybinds.keyOpenChair.wasPressed())
					OpenInventoryScreenPacket.send();
				
				// Allows for toggling the current seatbelt setting ingame
				if(WHCKeybinds.keySeatbelt.wasPressed() && !wasSeatbeltPressed)
				{
					SEATBELT_ON = !SEATBELT_ON;
					player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(SEATBELT_ON ? "on" : "off")));
					player.playSound(SEATBELT_ON ? WHCSoundEvents.SEATBELT_ON : WHCSoundEvents.SEATBELT_OFF, 1F, 0.5F + player.getRandom().nextFloat() * 0.5F);
					
					wasSeatbeltPressed = true;
				}
				else
					wasSeatbeltPressed = false;
			}
		});
	}
}