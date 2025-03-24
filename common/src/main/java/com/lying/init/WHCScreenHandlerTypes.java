package com.lying.init;

import com.lying.entity.WheelchairEntity;
import com.lying.reference.Reference;
import com.lying.screen.ChairInventoryScreenHandler;
import com.lying.screen.WalkerInventoryScreenHandler;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class WHCScreenHandlerTypes
{
	private static final DeferredRegister<ScreenHandlerType<?>> HANDLERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SCREEN_HANDLER);
	
	public static final RegistrySupplier<ScreenHandlerType<ChairInventoryScreenHandler>> WHEELCHAIR_INVENTORY_HANDLER	= register("wheelchair_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new ChairInventoryScreenHandler(syncId, playerInventory, (WheelchairEntity)playerInventory.player.getVehicle()), FeatureFlags.VANILLA_FEATURES));
	public static final RegistrySupplier<ScreenHandlerType<WalkerInventoryScreenHandler>> WALKER_INVENTORY_HANDLER	= register("walker_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new WalkerInventoryScreenHandler(syncId, playerInventory, new SimpleInventory(15)), FeatureFlags.VANILLA_FEATURES));
	
	private static <T extends ScreenHandler> RegistrySupplier<ScreenHandlerType<T>> register(String nameIn, ScreenHandlerType<T> typeIn)
	{
		return HANDLERS.register(Reference.ModInfo.prefix(nameIn), () -> typeIn);
	}
	
	public static void init()
	{
		HANDLERS.register();
	}
}
