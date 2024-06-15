package com.lying.init;

import java.util.function.Supplier;

import com.lying.entity.EntityWheelchair;
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
    
    /**
     * TODO Items for later versions
     * Cane crafting and variants
     * Crutch crafting and variants
     * 	NOTE Should be mainly plain items with specialised hand posing
     * 
     * AAC speech device? Would require a Lot of sound, UI, and texture work
     * 
     * Service animal vests! (req. Cardinal Components)
     * 	NOTE Should store animal in Chairspace if it would die
     */
    
    public static final RegistrySupplier<Item> CONTROLLER = register("controller", () -> new OnAStickItem<EntityWheelchair>(new Item.Settings().arch$tab(WHEELCHAIR_TAB), WHCEntityTypes.WHEELCHAIR.get(), 0));
    
    private static RegistrySupplier<Item> register(String nameIn, Supplier<Item> itemIn)
    {
    	return ITEMS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
    }
    
    public static void init()
    {
    	ITEMS.register();
		TABS.register();
    }
    
    private static RegistrySupplier<Item> wheelchair(String name)
    {
    	String fullName = name+"_wheelchair";
		WHCBlocks.registerFakeBlock(fullName);
    	return register(fullName, () -> new ItemWheelchair(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
    }
    
    private static RegistrySupplier<Item> wheel(String name)
    {
    	return register(name+"_wheel", () -> new Item(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(2)));
    }
}
