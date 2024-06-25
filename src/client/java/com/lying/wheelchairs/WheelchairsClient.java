package com.lying.wheelchairs;

import org.lwjgl.glfw.GLFW;

import com.lying.wheelchairs.config.ClientConfig;
import com.lying.wheelchairs.init.WHCBlocks;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItemsClient;
import com.lying.wheelchairs.init.WHCKeybinds;
import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.init.WHCScreenHandlerTypes;
import com.lying.wheelchairs.init.WHCSoundEvents;
import com.lying.wheelchairs.network.OpenInventoryScreenPacket;
import com.lying.wheelchairs.network.ParentedEntityPositionReceiver;
import com.lying.wheelchairs.network.WHCPacketHandler;
import com.lying.wheelchairs.renderer.entity.EntityWalkerRenderer;
import com.lying.wheelchairs.renderer.entity.EntityWheelchairRenderer;
import com.lying.wheelchairs.screen.ChairInventoryScreen;
import com.lying.wheelchairs.screen.WalkerInventoryScreen;
import com.lying.wheelchairs.utility.ClientBus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class WheelchairsClient implements ClientModInitializer
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ClientConfig config;
	
	public static boolean SEATBELT_ON = false;
	private static boolean wasSeatbeltPressed = false;
	private static int loginState = -1;
	
	public void onInitializeClient()
	{
		config = new ClientConfig(mc.runDirectory.getAbsolutePath() + "/config/WheelchairsClient.cfg");
		config.read();
		
		ClientBus.registerEventCallbacks();
		WHCItemsClient.registerItemColors();
		BlockRenderLayerMap.INSTANCE.putBlock(WHCBlocks.FROSTED_LAVA, RenderLayer.getCutout());
		WHCModelParts.init();
		EntityRendererRegistry.register(WHCEntityTypes.WHEELCHAIR, EntityWheelchairRenderer::new);
		EntityRendererRegistry.register(WHCEntityTypes.WALKER, EntityWalkerRenderer::new);
		WHCKeybinds.keyOpenChair = WHCKeybinds.make("open_chair", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C);
		WHCKeybinds.keySeatbelt = WHCKeybinds.make("seatbelt", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X);
		
		registerEventCallbacks();
		
		HandledScreens.register(WHCScreenHandlerTypes.WHEELCHAIR_INVENTORY_HANDLER, ChairInventoryScreen::new);
		HandledScreens.register(WHCScreenHandlerTypes.WALKER_INVENTORY_HANDLER, WalkerInventoryScreen::new);
		
		ClientPlayNetworking.registerGlobalReceiver(WHCPacketHandler.PARENTED_MOVE_ID, new ParentedEntityPositionReceiver());
	}
	
	public void registerEventCallbacks()
	{
		// Informs the player of their configured seatbelt setting when they log in
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { loginState = -1; });
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { loginState++; });
		ClientTickEvents.START_CLIENT_TICK.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(loginState == 0 && mc.player != null)
			{
				SEATBELT_ON = config.seatbeltAtBoot() && player.hasVehicle();
				if(config.seatbeltAtBoot())
					player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(SEATBELT_ON ? "on" : "off")));
				loginState++;
			}
		});
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(player != null)
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