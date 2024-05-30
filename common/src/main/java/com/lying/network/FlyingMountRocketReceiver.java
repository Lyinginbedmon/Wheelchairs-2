package com.lying.network;

import com.lying.entity.IFlyingMount;

import dev.architectury.networking.NetworkManager.NetworkReceiver;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class FlyingMountRocketReceiver implements NetworkReceiver
{
	public void receive(PacketByteBuf buf, PacketContext context)
	{
		ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
		Hand hand = buf.readEnumConstant(Hand.class);
		ItemStack stack = player.getStackInHand(hand);
		if(stack.getItem() != Items.FIREWORK_ROCKET)
			return;
		
		Entity vehicle;
		if((vehicle = player.getVehicle()) == null)
			return;
		else if(!(vehicle instanceof LivingEntity && vehicle instanceof IFlyingMount))
			return;
		
		context.queue(() -> 
		{
			Item item = stack.getItem();
			if(((IFlyingMount)vehicle).canUseRocketNow())
			{
				World world = player.getWorld();
				FireworkRocketEntity rocket = new FireworkRocketEntity(world, stack, (LivingEntity)vehicle);
				world.spawnEntity(rocket);
				if(!player.getAbilities().creativeMode)
					stack.decrement(1);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
			}
		});
	}
}
