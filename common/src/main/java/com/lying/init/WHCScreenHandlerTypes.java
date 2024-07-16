package com.lying.init;

import com.lying.entity.EntityWheelchair;
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
import net.minecraft.util.Identifier;

public class WHCScreenHandlerTypes
{
	public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SCREEN_HANDLER);
	
	public static final RegistrySupplier<ScreenHandlerType<ChairInventoryScreenHandler>> WHEELCHAIR_INVENTORY_HANDLER = 
			register("wheelchair_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new ChairInventoryScreenHandler(syncId, playerInventory, (EntityWheelchair)playerInventory.player.getVehicle()), FeatureFlags.VANILLA_FEATURES));
	public static final RegistrySupplier<ScreenHandlerType<WalkerInventoryScreenHandler>> WALKER_INVENTORY_HANDLER	=
			register("walker_inventory", new ScreenHandlerType<>((syncId, playerInventory) -> new WalkerInventoryScreenHandler(syncId, playerInventory, new SimpleInventory(15)), FeatureFlags.VANILLA_FEATURES));
	
	private static <T extends ScreenHandler> RegistrySupplier<ScreenHandlerType<T>> register(String nameIn, ScreenHandlerType<T> typeIn)
	{
		return SCREEN_HANDLERS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), () -> typeIn);
	}
	
	public static void init()
	{
		SCREEN_HANDLERS.register();
	}
}
