package com.lying.wheelchairs.network;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;

public class FlyingWheelchairRocketReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		Hand hand = buf.readEnumConstant(Hand.class);
		ItemStack stack = player.getStackInHand(hand);
		if(stack.getItem() != Items.FIREWORK_ROCKET)
			return;
		
		if(!player.hasVehicle())
			return;
		else if(player.getVehicle().getType() != WHCEntityTypes.WHEELCHAIR)
			return;
		
		server.execute(() -> 
		{
			Item item = stack.getItem();
			EntityWheelchair chair = (EntityWheelchair)player.getVehicle();
//			if(chair.isFlying())	// FIXME Ensure this only fires IF the wheelchair is in fall-flying state
			{
				FireworkRocketEntity rocket = new FireworkRocketEntity(chair.getWorld(), stack, chair);
				chair.getWorld().spawnEntity(rocket);
				if(!player.getAbilities().creativeMode)
					stack.decrement(1);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
			}
		});
	}
}
