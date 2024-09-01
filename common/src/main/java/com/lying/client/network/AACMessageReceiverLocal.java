package com.lying.client.network;

import com.lying.client.WheelchairsClient;
import com.lying.reference.Reference;
import com.mojang.text2speech.Narrator;

import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;

public class AACMessageReceiverLocal implements NetworkReceiver
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public void receive(PacketByteBuf buf, PacketContext responseSender)
	{
		MutableText value = buf.readText().copy().styled(style -> style.withClickEvent(null));
		
		PlayerEntity sender = mc.world.getPlayerByUuid(buf.readUuid());
		MutableText senderName = sender == null ? buf.readText().copy() : sender.getDisplayName().copy();
		final MutableText message = Reference.ModInfo.translate("aac","message", senderName, value).copy();
		
		if(mc.options.getChatVisibility().getValue() != ChatVisibility.FULL)
			return;
		
		mc.inGameHud.getChatHud().addMessage(message);
		if(WheelchairsClient.config.shouldNarrateAAC() && sender != mc.player && sender != null)
			if(mc.player.distanceTo(sender) < 16D)
			{
				Narrator narrator = Narrator.getNarrator();
				narrator.clear();
				narrator.say(value.getString(), true);
			}
	}
}
