package com.lying.client;

import com.lying.client.config.ClientConfig;
import com.lying.client.init.WHCItemsClient;
import com.lying.client.init.WHCKeybinds;
import com.lying.client.init.WHCModelParts;
import com.lying.client.network.OpenInventoryScreenPacket;
import com.lying.client.screen.ChairInventoryScreen;
import com.lying.client.utility.ClientBus;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCScreenHandlerTypes;
import com.lying.init.WHCSoundEvents;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public final class WheelchairsClient
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ClientConfig config;
	
	public static boolean SEATBELT_ON = false;
	public static boolean wasSeatbeltPressed = false;
	public static int loginState = -1;
	
	public static void clientInit()
	{
		config = new ClientConfig(mc.runDirectory.getAbsolutePath() + "/config/WheelchairsClient.cfg");
		config.read();
		
		ClientBus.registerEventCallbacks();
		WHCItemsClient.registerItemColors();
		RenderTypeRegistry.register(RenderLayer.getCutout(), WHCBlocks.FROSTED_LAVA);
		WHCModelParts.init();
		
		WHCKeybinds.init();
		registerEventCallbacks();
		
		MenuRegistry.registerScreenFactory(WHCScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, ChairInventoryScreen::new);
	}
	
	public static void registerEventCallbacks()
	{
		// Informs the player of their configured seatbelt setting when they log in
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> { WheelchairsClient.loginState = -1; });
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> { WheelchairsClient.loginState++; });
		ClientTickEvent.CLIENT_PRE.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(WheelchairsClient.loginState == 0 && mc.player != null)
			{
				WheelchairsClient.SEATBELT_ON = WheelchairsClient.config.seatbeltAtBoot() && player.hasVehicle();
				if(WheelchairsClient.config.seatbeltAtBoot())
					player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(WheelchairsClient.SEATBELT_ON ? "on" : "off")));
				WheelchairsClient.loginState++;
			}
		});
		
		ClientTickEvent.CLIENT_POST.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(player != null && player.hasVehicle())
			{
				// Allows for opening wheelchair inventory
				while(WHCKeybinds.keyOpenChair.wasPressed())
					OpenInventoryScreenPacket.send();
				
				// Allows for toggling the current seatbelt setting ingame
				if(WHCKeybinds.keySeatbelt.wasPressed() && !WheelchairsClient.wasSeatbeltPressed)
				{
					WheelchairsClient.SEATBELT_ON = !WheelchairsClient.SEATBELT_ON;
					player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(WheelchairsClient.SEATBELT_ON ? "on" : "off")));
					player.playSound(WheelchairsClient.SEATBELT_ON ? WHCSoundEvents.SEATBELT_ON : WHCSoundEvents.SEATBELT_OFF, 1F, 0.5F + player.getRandom().nextFloat() * 0.5F);
					
					WheelchairsClient.wasSeatbeltPressed = true;
				}
				else
					WheelchairsClient.wasSeatbeltPressed = false;
			}
		});
	}
}
