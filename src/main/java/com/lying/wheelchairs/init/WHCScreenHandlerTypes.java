package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.screen.ChairInventoryScreenHandler;
import com.lying.wheelchairs.screen.WalkerInventoryScreenHandler;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class WHCScreenHandlerTypes
{
	private static final Map<Identifier, ScreenHandlerType<?>> HANDLERS = new HashMap<>();
	
	public static final ScreenHandlerType<ChairInventoryScreenHandler> WHEELCHAIR_INVENTORY_HANDLER	= register("wheelchair_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new ChairInventoryScreenHandler(syncId, playerInventory, (EntityWheelchair)playerInventory.player.getVehicle()), FeatureFlags.VANILLA_FEATURES));
	public static final ScreenHandlerType<WalkerInventoryScreenHandler> WALKER_INVENTORY_HANDLER	= register("walker_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new WalkerInventoryScreenHandler(syncId, playerInventory, new SimpleInventory(15)), FeatureFlags.VANILLA_FEATURES));
	
	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String nameIn, ScreenHandlerType<T> typeIn)
	{
		HANDLERS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), typeIn);
		return typeIn;
	}
	
	public static void init()
	{
		HANDLERS.forEach((name,handler) -> Registry.register(Registries.SCREEN_HANDLER, name, handler));
	}
}
