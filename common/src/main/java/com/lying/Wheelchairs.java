package com.lying;

import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.config.ServerConfig;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCEnchantments;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCScreenHandlerTypes;
import com.lying.init.WHCSoundEvents;
import com.lying.init.WHCSpecialRecipes;
import com.lying.init.WHCUpgrades;
import com.lying.network.AACMessageReceiver;
import com.lying.network.FlyingMountRocketReceiver;
import com.lying.network.ForceUnparentReceiver;
import com.lying.network.OpenInventoryScreenReceiver;
import com.lying.network.StartFlyingReceiver;
import com.lying.network.WHCPacketHandler;
import com.lying.reference.Reference;
import com.lying.utility.ServerBus;
import com.lying.utility.XPlatHandler;

import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Wheelchairs
{
	public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
	
	public static ServerConfig config;
	
	public static BiConsumer<PlayerEntity, ItemStack> openAACScreen = (player, stack) -> {};
	
	public static XPlatHandler HANDLER = new XPlatHandler() 
	{
		public boolean hasVest(LivingEntity entity) { return false; }
		
		public ItemStack getVest(LivingEntity entity) { return ItemStack.EMPTY; }
		
		public void setVest(LivingEntity entity, ItemStack stack) { }
	};
	
	public static void commonInit()
	{
		config = new ServerConfig("config/Wheelchairs.cfg");
		config.read();
		LOGGER.info("Reading Wheelchairs server config:");
		LOGGER.info(" * Sword cane setting: "+config.swordCaneFilter().name());
		LOGGER.info(" * Handsy walkers setting: "+config.handsyWalkers());
		
		ServerBus.registerEventCallbacks();
		WHCEntityTypes.init();
		WHCItems.init();
		WHCUpgrades.init();
		WHCBlocks.init();
		WHCEnchantments.init();
		WHCSpecialRecipes.init();
		WHCChairspaceConditions.init();
		WHCScreenHandlerTypes.init();
		WHCSoundEvents.init();
		
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.OPEN_INVENTORY_ID, new OpenInventoryScreenReceiver());
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.FLYING_START_ID, new StartFlyingReceiver());
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.FLYING_ROCKET_ID, new FlyingMountRocketReceiver());
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.FORCE_UNPARENT_ID, new ForceUnparentReceiver());
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.AAC_MESSAGE_SEND_ID, new AACMessageReceiver());
	}
	
	public static void openAACScreen(PlayerEntity player, ItemStack heldStack) { openAACScreen.accept(player, heldStack); }
}