package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.screen.ChairInventoryScreenHandler;

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
	
	public static final ScreenHandlerType<ChairInventoryScreenHandler> INVENTORY_SCREEN_HANDLER = register("inventory_screen", new ScreenHandlerType<>((syncId, playerInventory) -> new ChairInventoryScreenHandler(syncId, playerInventory, new SimpleInventory(15)), FeatureFlags.VANILLA_FEATURES));
	
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
