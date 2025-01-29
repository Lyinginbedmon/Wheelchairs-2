package com.lying.forge.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.forge.WheelchairsForge;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncVest
{
	private UUID entityID = null;
	private ItemStack vestStack = ItemStack.EMPTY;
	
	public PacketSyncVest(){ }
	public PacketSyncVest(UUID mapName, ItemStack stackIn)
	{
		entityID = mapName;
		vestStack = stackIn.copy();
	}
	
	public static PacketSyncVest decode(PacketByteBuf par1Buffer)
	{
		PacketSyncVest packet = new PacketSyncVest();
		packet.entityID = par1Buffer.readUuid();
		packet.vestStack = par1Buffer.readItemStack();
		return packet;
	}
	
	public static void encode(PacketSyncVest msg, PacketByteBuf par1Buffer)
	{
		par1Buffer.writeUuid(msg.entityID);
		par1Buffer.writeItemStack(msg.vestStack);
	}
	
	public static void handle(PacketSyncVest msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isClient())
		{
			PlayerEntity localPlayer = WheelchairsForge.getLocalPlayer.get();
			if(localPlayer == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before player entity created #");
				return;
			}
			
			World world = localPlayer.getWorld();
			if(world == null)
			{
				Wheelchairs.LOGGER.error("# Tried to synchronise a service animal vest before world set #");
				return;
			}
			else
				world.getEntitiesByClass(LivingEntity.class, localPlayer.getBoundingBox().expand(5000D), e -> e.getUuid().equals(msg.entityID)).forEach(e -> Wheelchairs.HANDLER.setVest(e, msg.vestStack));
		}
		
		context.setPacketHandled(true);
	}
}
