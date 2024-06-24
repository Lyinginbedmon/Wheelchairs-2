package com.lying.wheelchairs.network;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ParentedEntityInputReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		float strafe = buf.readFloat();
		float forward = buf.readFloat();
		boolean jump = buf.readBoolean();
		boolean sneak = buf.readBoolean();
		server.execute(() -> IParentedEntity.getParentedEntitiesOf(player).forEach(child -> onParentInput(child, player, strafe, forward, jump, sneak)));
	}
	
	private static <T extends LivingEntity & IParentedEntity> void onParentInput(T child, LivingEntity parent, float sideways, float forward, boolean jump, boolean sneak)
	{
		if(sideways >= -1F && sideways <= 1F)
			child.sidewaysSpeed = sideways;
		
		if(forward >= -1F && forward <= 1F)
			child.forwardSpeed = forward;
		
		child.setJumping(jump);
		child.setSneaking(sneak);
	}
}
