package com.lying.init;

import com.lying.reference.Reference;
import com.lying.screen.ChairInventoryScreenHandler;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class WHCScreenHandlerTypes
{
	public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SCREEN_HANDLER);
	
	public static final RegistrySupplier<ScreenHandlerType<ChairInventoryScreenHandler>> INVENTORY_SCREEN_HANDLER = register("inventory_screen", new ScreenHandlerType<>((syncId, playerInventory) -> new ChairInventoryScreenHandler(syncId, playerInventory, new SimpleInventory(15)), FeatureFlags.VANILLA_FEATURES));
	
	private static <T extends ScreenHandler> RegistrySupplier<ScreenHandlerType<T>> register(String nameIn, ScreenHandlerType<T> typeIn)
	{
		return SCREEN_HANDLERS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), () -> typeIn);
	}
	
	public static void init()
	{
		SCREEN_HANDLERS.register();
	}
}
