package com.lying.client;

import com.lying.Wheelchairs;
import com.lying.client.config.ClientConfig;
import com.lying.client.init.WHCKeybinds;
import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.feature.CatVestLayer;
import com.lying.client.renderer.entity.feature.FoxVestLayer;
import com.lying.client.renderer.entity.feature.ParrotVestLayer;
import com.lying.client.renderer.entity.feature.WolfVestLayer;
import com.lying.client.screen.AACScreen;
import com.lying.client.utility.AACLibrary;
import com.lying.client.utility.ClientBus;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCSoundEvents;
import com.lying.network.AACMessagePacket;
import com.lying.network.OpenInventoryScreenPacket;
import com.lying.network.SetSeatbeltPacket;
import com.lying.reference.Reference;
import com.mojang.text2speech.Narrator;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.render.entity.state.CatEntityRenderState;
import net.minecraft.client.render.entity.state.FoxEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.ParrotEntityRenderState;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class WheelchairsClient
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ClientConfig config;
	
	public static boolean SEATBELT_ON = false;
	public static boolean wasSeatbeltPressed = false;
	public static int loginState = -1;
	
	public static void clientInit()
	{
		Wheelchairs.openAACScreen = (player, stack) -> { mc.setScreen(new AACScreen(stack.getName())); };
		
		config = new ClientConfig(mc.runDirectory.getAbsolutePath() + "/config/WheelchairsClient.cfg");
		config.read();
		Wheelchairs.LOGGER.info("Reading Wheelchairs client config:");
		Wheelchairs.LOGGER.info(" * Seatbelt login setting: "+config.seatbeltAtBoot());
		Wheelchairs.LOGGER.info(" * AAC proximity narration: "+config.shouldNarrateAAC());
		AACLibrary.init();
		
		ClientBus.registerEventCallbacks();
		RenderTypeRegistry.register(RenderLayer.getCutout(), WHCBlocks.FROSTED_LAVA.get());
		WHCModelParts.init((layer, definition) -> EntityModelLayerRegistry.register(layer, definition));
		
		registerEventCallbacks();
		registerS2CPacketReceivers();
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
					mc.inGameHud.getChatHud().addMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(WheelchairsClient.SEATBELT_ON ? "on" : "off")));
				WheelchairsClient.loginState++;
			}
		});
		
		ClientTickEvent.CLIENT_POST.register(client -> 
		{
			ClientPlayerEntity player = mc.player;
			if(player != null)
			{
				// Allows for opening wheelchair inventory
				while(WHCKeybinds.keyOpenChair.wasPressed())
					OpenInventoryScreenPacket.send(mc.targetedEntity != null ? mc.targetedEntity.getUuid() : null);
				
				// Allows for toggling the current seatbelt setting ingame
				if(WHCKeybinds.keySeatbelt.wasPressed() && !WheelchairsClient.wasSeatbeltPressed && player.hasVehicle())
				{
					WheelchairsClient.SEATBELT_ON = !WheelchairsClient.SEATBELT_ON;
					SetSeatbeltPacket.send(WheelchairsClient.SEATBELT_ON);
					player.playSound(WheelchairsClient.SEATBELT_ON ? WHCSoundEvents.SEATBELT_ON.get() : WHCSoundEvents.SEATBELT_OFF.get(), 1F, 0.5F + player.getRandom().nextFloat() * 0.5F);
					WheelchairsClient.wasSeatbeltPressed = true;
				}
				else
					WheelchairsClient.wasSeatbeltPressed = false;
			}
		});
	}
	
	private static void registerS2CPacketReceivers()
	{
		Wheelchairs.LOGGER.info(" # Registered client-side packet receivers");
		NetworkManager.registerReceiver(NetworkManager.s2c(), AACMessagePacket.Payload.Receive.PACKET_TYPE, AACMessagePacket.Payload.Receive.PACKET_CODEC, (value, context) -> 
		{
			if(mc.options.getChatVisibility().getValue() != ChatVisibility.FULL)
				return;
			
			MutableText text = value.message().copy().styled(style -> style.withClickEvent(null));
			PlayerEntity sender = context.getPlayer().getWorld().getPlayerByUuid(value.playerID());
			MutableText senderName = (sender == null ? value.playerName() : sender.getDisplayName()).copy();
			final MutableText message = Reference.ModInfo.translate("aac", "message", senderName, text).copy();
			mc.inGameHud.getChatHud().addMessage(message);
			if(WheelchairsClient.config.shouldNarrateAAC() && sender != mc.player && sender != null)
				if(mc.player.distanceTo(sender) < 16D)
				{
					Narrator narrator = Narrator.getNarrator();
					narrator.clear();
					narrator.say(text.getString(), true);
				}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity, C extends LivingEntityRenderState, M extends EntityModel<C>> FeatureRenderer<C, M> getVestFeatureForType(EntityType<T> entityType, LivingEntityRenderer<T, C, M> renderer)
	{
		if(entityType == EntityType.WOLF)
			return (FeatureRenderer<C, M>) new WolfVestLayer((LivingEntityRenderer<WolfEntity, WolfEntityRenderState, WolfEntityModel>)renderer);
		else if(entityType == EntityType.CAT)
			return (FeatureRenderer<C, M>) new CatVestLayer((LivingEntityRenderer<CatEntity, CatEntityRenderState, CatEntityModel>)renderer);
		else if(entityType == EntityType.PARROT)
			return (FeatureRenderer<C, M>) new ParrotVestLayer((LivingEntityRenderer<ParrotEntity, ParrotEntityRenderState, ParrotEntityModel>)renderer);
		else if(entityType == EntityType.FOX)
			return (FeatureRenderer<C, M>) new FoxVestLayer((LivingEntityRenderer<FoxEntity, FoxEntityRenderState, FoxEntityModel>)renderer);
		return null;
	}
}
