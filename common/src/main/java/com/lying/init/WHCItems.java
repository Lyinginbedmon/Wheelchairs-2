package com.lying.init;

import java.util.function.Supplier;

import com.lying.entity.EntityWheelchair;
import com.lying.item.ItemCane;
import com.lying.item.ItemCaneHandle;
import com.lying.item.ItemCrutch;
import com.lying.item.ItemStool;
import com.lying.item.ItemWalker;
import com.lying.item.ItemWheelchair;
import com.lying.reference.Reference;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.OnAStickItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WHCItems
{
	public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM_GROUP);
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM);
	
	public static final RegistrySupplier<ItemGroup> WHEELCHAIR_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(WHCItems.WHEELCHAIR_OAK.get())));
	
	public static final RegistrySupplier<Item> WHEELCHAIR_OAK = wheelchair("oak");
	public static final RegistrySupplier<Item> WHEELCHAIR_SPRUCE = wheelchair("spruce");
	public static final RegistrySupplier<Item> WHEELCHAIR_BIRCH = wheelchair("birch");
	public static final RegistrySupplier<Item> WHEELCHAIR_DARK_OAK = wheelchair("dark_oak");
	public static final RegistrySupplier<Item> WHEELCHAIR_ACACIA = wheelchair("acacia");
	public static final RegistrySupplier<Item> WHEELCHAIR_JUNGLE = wheelchair("jungle");
	public static final RegistrySupplier<Item> WHEELCHAIR_CRIMSON = wheelchair("crimson");
	public static final RegistrySupplier<Item> WHEELCHAIR_WARPED = wheelchair("warped");
	public static final RegistrySupplier<Item> WHEELCHAIR_MANGROVE = wheelchair("mangrove");
	public static final RegistrySupplier<Item> WHEELCHAIR_CHERRY = wheelchair("cherry");
	public static final RegistrySupplier<Item> WHEELCHAIR_BAMBOO = wheelchair("bamboo");
	
	public static final RegistrySupplier<Item> WHEEL_OAK = wheel("oak");
	public static final RegistrySupplier<Item> WHEEL_SPRUCE = wheel("spruce");
	public static final RegistrySupplier<Item> WHEEL_BIRCH = wheel("birch");
	public static final RegistrySupplier<Item> WHEEL_DARK_OAK = wheel("dark_oak");
	public static final RegistrySupplier<Item> WHEEL_ACACIA = wheel("acacia");
	public static final RegistrySupplier<Item> WHEEL_JUNGLE = wheel("jungle");
	public static final RegistrySupplier<Item> WHEEL_CRIMSON = wheel("crimson");
	public static final RegistrySupplier<Item> WHEEL_WARPED = wheel("warped");
	public static final RegistrySupplier<Item> WHEEL_MANGROVE = wheel("mangrove");
	public static final RegistrySupplier<Item> WHEEL_CHERRY = wheel("cherry");
	public static final RegistrySupplier<Item> WHEEL_BAMBOO = wheel("bamboo");
	public static final RegistrySupplier<Item> WHEEL_COPPER = wheel("copper");
	public static final RegistrySupplier<Item> WHEEL_IRON = wheel("iron");
	public static final RegistrySupplier<Item> WHEEL_GOLD = wheel("gold");
	public static final RegistrySupplier<Item> WHEEL_NETHERITE = register("netherite_wheel", () -> new Item(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(2).fireproof()));
	
	/**
	 * TODO Items for later versions
	 * AAC speech device? Would require a Lot of sound, UI, and texture work
	 * 
	 * Service animal vests! (req. Cardinal Components)
	 * 	NOTE Should store animal in Chairspace if it would die
	 */
	
	public static final RegistrySupplier<Item> CRUTCH_OAK = crutch("oak");
	public static final RegistrySupplier<Item> CRUTCH_SPRUCE = crutch("spruce");
	public static final RegistrySupplier<Item> CRUTCH_BIRCH = crutch("birch");
	public static final RegistrySupplier<Item> CRUTCH_DARK_OAK = crutch("dark_oak");
	public static final RegistrySupplier<Item> CRUTCH_ACACIA = crutch("acacia");
	public static final RegistrySupplier<Item> CRUTCH_JUNGLE = crutch("jungle");
	public static final RegistrySupplier<Item> CRUTCH_CRIMSON = crutch("crimson");
	public static final RegistrySupplier<Item> CRUTCH_WARPED = crutch("warped");
	public static final RegistrySupplier<Item> CRUTCH_MANGROVE = crutch("mangrove");
	public static final RegistrySupplier<Item> CRUTCH_CHERRY = crutch("cherry");
	public static final RegistrySupplier<Item> CRUTCH_BAMBOO = crutch("bamboo");
	
	public static final RegistrySupplier<Item> WALKER_OAK = walker("oak");
	public static final RegistrySupplier<Item> WALKER_SPRUCE = walker("spruce");
	public static final RegistrySupplier<Item> WALKER_BIRCH = walker("birch");
	public static final RegistrySupplier<Item> WALKER_DARK_OAK = walker("dark_oak");
	public static final RegistrySupplier<Item> WALKER_ACACIA = walker("acacia");
	public static final RegistrySupplier<Item> WALKER_JUNGLE = walker("jungle");
	public static final RegistrySupplier<Item> WALKER_CRIMSON = walker("crimson");
	public static final RegistrySupplier<Item> WALKER_WARPED = walker("warped");
	public static final RegistrySupplier<Item> WALKER_MANGROVE = walker("mangrove");
	public static final RegistrySupplier<Item> WALKER_CHERRY = walker("cherry");
	public static final RegistrySupplier<Item> WALKER_BAMBOO = walker("bamboo");
	
	public static final RegistrySupplier<Item> CANE_OAK = cane("oak");
	public static final RegistrySupplier<Item> CANE_SPRUCE = cane("spruce");
	public static final RegistrySupplier<Item> CANE_BIRCH = cane("birch");
	public static final RegistrySupplier<Item> CANE_DARK_OAK = cane("dark_oak");
	public static final RegistrySupplier<Item> CANE_ACACIA = cane("acacia");
	public static final RegistrySupplier<Item> CANE_JUNGLE = cane("jungle");
	public static final RegistrySupplier<Item> CANE_CRIMSON = cane("crimson");
	public static final RegistrySupplier<Item> CANE_WARPED = cane("warped");
	public static final RegistrySupplier<Item> CANE_MANGROVE = cane("mangrove");
	public static final RegistrySupplier<Item> CANE_CHERRY = cane("cherry");
	public static final RegistrySupplier<Item> CANE_BAMBOO = cane("bamboo");

	public static final RegistrySupplier<Item> HANDLE_COPPER = handle("copper");
	public static final RegistrySupplier<Item> HANDLE_IRON = handle("iron");
	public static final RegistrySupplier<Item> HANDLE_GOLD = handle("gold");
	public static final RegistrySupplier<Item> HANDLE_SKULL = handle("skull");
	public static final RegistrySupplier<Item> HANDLE_WITHER = handle("wither_skull");
	public static final RegistrySupplier<Item> HANDLE_BONE = handle("bone");
	public static final RegistrySupplier<Item> HANDLE_OAK = handle("oak");
	public static final RegistrySupplier<Item> HANDLE_SPRUCE = handle("spruce");
	public static final RegistrySupplier<Item> HANDLE_BIRCH = handle("birch");
	public static final RegistrySupplier<Item> HANDLE_DARK_OAK = handle("dark_oak");
	public static final RegistrySupplier<Item> HANDLE_ACACIA = handle("acacia");
	public static final RegistrySupplier<Item> HANDLE_JUNGLE = handle("jungle");
	public static final RegistrySupplier<Item> HANDLE_CRIMSON = handle("crimson");
	public static final RegistrySupplier<Item> HANDLE_WARPED = handle("warped");
	public static final RegistrySupplier<Item> HANDLE_MANGROVE = handle("mangrove");
	public static final RegistrySupplier<Item> HANDLE_CHERRY = handle("cherry");
	public static final RegistrySupplier<Item> HANDLE_BAMBOO = handle("bamboo");
	
	public static final RegistrySupplier<Item> CONTROLLER = register("controller", () -> new OnAStickItem<EntityWheelchair>(new Item.Settings().arch$tab(WHEELCHAIR_TAB), WHCEntityTypes.WHEELCHAIR.get(), 0));
	public static final RegistrySupplier<Item> STOOL	= registerWithFake("wheeled_stool", () -> new ItemStool(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
	
	private static RegistrySupplier<Item> register(String nameIn, Supplier<Item> itemIn)
	{
		return ITEMS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
	}
	
	private static RegistrySupplier<Item> registerWithFake(String nameIn, Supplier<Item> itemIn)
	{
		WHCBlocks.registerFakeBlock(nameIn);
		return ITEMS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
	}
	
	public static void init()
	{
		WHCBlocks.registerFakeBlock("walker_chest");
		
		ITEMS.register();
		TABS.register();
	}
	
	private static RegistrySupplier<Item> wheelchair(String name)
	{
		return registerWithFake(name+"_wheelchair", () -> new ItemWheelchair(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
	}
	
	private static RegistrySupplier<Item> walker(String name)
	{
		return registerWithFake(name+"_walker", () -> new ItemWalker(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
	}
	
	private static RegistrySupplier<Item> wheel(String name)
	{
		return register(name+"_wheel", () -> new Item(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(2)));
	}
	
	private static RegistrySupplier<Item> crutch(String name)
	{
		return register(name+"_crutch", () -> new ItemCrutch(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
	}
	
	private static RegistrySupplier<Item> cane(String name)
	{
		return register(name+"_cane", () -> new ItemCane(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
	}
	
	private static RegistrySupplier<Item> handle(String name)
	{
		return register(name+"_handle", () -> new ItemCaneHandle(new Item.Settings().maxCount(1)));
	}
}
