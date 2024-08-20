package com.lying.wheelchairs.network;

import com.lying.wheelchairs.WheelchairsClient;
import com.mojang.text2speech.Narrator;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class AACMessageReceiverLocal implements PlayChannelHandler
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		MutableText value = buf.readText().copy().styled(style -> style.withClickEvent(null));
		
		PlayerEntity player = mc.world.getPlayerByUuid(buf.readUuid());
		MutableText playerName = player == null ? buf.readText().copy() : player.getDisplayName().copy();
		final MutableText message = Text.translatable("aac.wheelchairs.message", playerName, value);
		
		if(mc.options.getChatVisibility().getValue() != ChatVisibility.FULL)
			return;
		
		mc.inGameHud.getChatHud().addMessage(message);
		if(WheelchairsClient.config.shouldNarrateAAC() && player != mc.player && player != null)
			if(mc.player.distanceTo(player) < 16D)
			{
				Narrator narrator = Narrator.getNarrator();
				narrator.clear();
				narrator.say(value.getString(), true);
			}
	}
}
