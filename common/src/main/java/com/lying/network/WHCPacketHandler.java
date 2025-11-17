package com.lying.network;

import java.util.UUID;

import com.lying.Wheelchairs;
import com.lying.entity.ChairUpgrade;
import com.lying.entity.IFlyingMount;
import com.lying.entity.WalkerEntity;
import com.lying.entity.WheelchairEntity;
import com.lying.init.WHCEntityTypes;
import com.lying.reference.Reference;
import com.lying.screen.ChairInventoryScreenHandler;
import com.lying.screen.WalkerInventoryScreenHandler;
import com.lying.utility.ServerBus;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WHCPacketHandler
{
	public static final Identifier OPEN_INVENTORY_ID	= make("open_inventory_screen");
	public static final Identifier FLYING_START_ID		= make("flying_start");
	public static final Identifier FLYING_ROCKET_ID		= make("flying_rocket");
	public static final Identifier FORCE_UNPARENT_ID	= make("force_unparent");
	public static final Identifier AAC_MESSAGE_ID		= make("aac_message");
	public static final Identifier SET_SEATBELT_ID		= make("set_seatbelt");
	
	private static Identifier make(String nameIn) { return Reference.ModInfo.prefix(nameIn); }
	
	public static void initServer()
	{
		Wheelchairs.LOGGER.info(" # Registered server-side packet receivers");
		NetworkManager.registerReceiver(NetworkManager.c2s(), AACMessagePacket.Payload.Send.PACKET_TYPE, AACMessagePacket.Payload.Send.PACKET_CODEC, (value, context) -> 
			context.getPlayer().getServer().getPlayerManager().getPlayerList().forEach(p -> AACMessagePacket.sendToPlayer(p, value)));
		initKeyHandling();
		initUpgradeHandling();
	}
	
	private static void initKeyHandling()
	{
		NetworkManager.registerReceiver(NetworkManager.c2s(), FlyingMountRocketPacket.PACKET_TYPE, FlyingMountRocketPacket.PACKET_CODEC, (value, context) -> 
		{
			ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
			ItemStack stack = player.getStackInHand(value.hand());
			if(stack.getItem() != Items.FIREWORK_ROCKET)
				return;
			
			Entity vehicle;
			if((vehicle = player.getVehicle()) == null)
				return;
			else if(!(vehicle instanceof LivingEntity && vehicle instanceof IFlyingMount))
				return;
			
			player.getServer().execute(() -> 
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
		});
		
		NetworkManager.registerReceiver(NetworkManager.c2s(), StartFlyingPacket.PACKET_TYPE, StartFlyingPacket.PACKET_CODEC, (value, context) -> 
		{
			ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
			if(player.hasVehicle() && player.getVehicle() instanceof IFlyingMount)
				player.getServer().execute(() -> ((IFlyingMount)player.getVehicle()).startFlying());
		});
		
		NetworkManager.registerReceiver(NetworkManager.c2s(), SetSeatbeltPacket.PACKET_TYPE, SetSeatbeltPacket.PACKET_CODEC, (value, context) -> 
		{
			ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
			UUID id = player.getUuid();
			boolean currentSetting = ServerBus.getSeatbelt(id);
			ServerBus.setSeatbelt(id, value.setting() && player.hasVehicle());
			if(ServerBus.getSeatbelt(id) != currentSetting)
				player.sendMessage(Text.translatable("gui.wheelchairs.seatbelt_"+(!currentSetting ? "on" : "off")));
		});
	}
	
	private static void initUpgradeHandling()
	{
		NetworkManager.registerReceiver(NetworkManager.c2s(), ForceUnparentPacket.PACKET_TYPE, ForceUnparentPacket.PACKET_CODEC, (value, context) -> 
		{
			ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
			player.getServer().execute(() -> 
			{
				if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get() && ((WheelchairEntity)player.getVehicle()).hasParent())
					((WheelchairEntity)player.getVehicle()).forceUnbind();
			});
		});
		
		NetworkManager.registerReceiver(NetworkManager.c2s(), OpenInventoryScreenPacket.PACKET_TYPE, OpenInventoryScreenPacket.PACKET_CODEC, (value, context) -> 
		{
			ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
			// If in wheelchair, open wheelchair inventory
			if(player.hasVehicle() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR.get() && ((WheelchairEntity)player.getVehicle()).getUpgrades().stream().anyMatch(ChairUpgrade::enablesScreen))
			{
				WheelchairEntity vehicle = (WheelchairEntity)player.getVehicle();
				MenuRegistry.openMenu(player, new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ChairInventoryScreenHandler(id, playerInventory, vehicle), vehicle.getDisplayName()));
				return;
			}
			
			// Else if player was looking at an entity, try to open its inventory
			if(value.openTarget())
			{
				UUID uuid = value.entityID();
				// Check all walkers with an inventory within 4 blocks for a matching UUID
				player.getWorld().getEntitiesByType(WHCEntityTypes.WALKER.get(), player.getBoundingBox().expand(4D), WalkerEntity::hasInventory).stream()
					.filter(e -> e.getUuid().equals(uuid)).findFirst()
					.ifPresent(walker ->
						MenuRegistry.openMenu(player, new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new WalkerInventoryScreenHandler(id, playerInventory, walker.getInventory(), walker), walker.getDisplayName())));
			}
		});
	}
}
