package com.lying;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.init.WHCBlocks;
import com.lying.init.WHCEnchantments;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCScreenHandlerTypes;
import com.lying.init.WHCSoundEvents;
import com.lying.init.WHCSpecialRecipes;
import com.lying.init.WHCUpgrades;
import com.lying.network.FlyingMountRocketReceiver;
import com.lying.network.OpenInventoryScreenReceiver;
import com.lying.network.StartFlyingReceiver;
import com.lying.network.WHCPacketHandler;
import com.lying.reference.Reference;
import com.lying.utility.ServerBus;

import dev.architectury.networking.NetworkManager;

public class Wheelchairs
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
	
	public static void commonInit()
	{
		WHCEntityTypes.init();
		WHCItems.init();
		WHCUpgrades.init();
		WHCEnchantments.init();
		WHCBlocks.init();
		WHCSpecialRecipes.init();
		WHCScreenHandlerTypes.init();
		WHCSoundEvents.init();
		
		ServerBus.registerEventCallbacks();
        
    	NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.OPEN_INVENTORY_ID, new OpenInventoryScreenReceiver());
    	NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.FLYING_START_ID, new StartFlyingReceiver());
    	NetworkManager.registerReceiver(NetworkManager.Side.C2S, WHCPacketHandler.FLYING_ROCKET_ID, new FlyingMountRocketReceiver());
	}
}