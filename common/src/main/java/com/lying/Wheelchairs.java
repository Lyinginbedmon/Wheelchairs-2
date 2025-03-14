package com.lying;

import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.config.ServerConfig;
import com.lying.init.WHCBlocks;
import com.lying.init.WHCChairspaceConditions;
import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCEnchantments;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCScreenHandlerTypes;
import com.lying.init.WHCSoundEvents;
import com.lying.init.WHCSpecialRecipes;
import com.lying.init.WHCUpgrades;
import com.lying.network.WHCPacketHandler;
import com.lying.reference.Reference;
import com.lying.utility.ServerBus;
import com.lying.utility.XPlatHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Wheelchairs
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
    public static ServerConfig config;
    
    public static BiConsumer<PlayerEntity, ItemStack> openAACScreen = (player, stack) -> {};
    
    public static XPlatHandler HANDLER = new XPlatHandler() { };
    
	public static void commonInit()
	{
		config = new ServerConfig("config/Wheelchairs.cfg");
		config.read();
		LOGGER.info("Sword cane config setting: {}", config.swordCaneFilter().name());
		
		WHCChairspaceConditions.init();
		ServerBus.registerEventCallbacks();
		WHCDataComponentTypes.init();
		WHCItems.init();
		WHCUpgrades.init();
		WHCEnchantments.init();
		WHCBlocks.init();
		WHCEntityTypes.init();
		WHCSpecialRecipes.init();
		WHCScreenHandlerTypes.init();
		WHCSoundEvents.init();
		
		WHCPacketHandler.initServer();
	}
	
	public static void openAACScreen(PlayerEntity player, ItemStack heldStack) { openAACScreen.accept(player, heldStack); }
}