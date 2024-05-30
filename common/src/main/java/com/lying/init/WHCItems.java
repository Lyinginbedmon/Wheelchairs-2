package com.lying.init;

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
    		() -> new ItemStack(WHCItems.WHEELCHAIR_OAK)));
    
    public static final Item WHEELCHAIR_OAK = wheelchair("oak");
    public static final Item WHEELCHAIR_SPRUCE = wheelchair("spruce");
    public static final Item WHEELCHAIR_BIRCH = wheelchair("birch");
    public static final Item WHEELCHAIR_DARK_OAK = wheelchair("dark_oak");
    public static final Item WHEELCHAIR_ACACIA = wheelchair("acacia");
    public static final Item WHEELCHAIR_JUNGLE = wheelchair("jungle");
    public static final Item WHEELCHAIR_CRIMSON = wheelchair("crimson");
    public static final Item WHEELCHAIR_WARPED = wheelchair("warped");
    public static final Item WHEELCHAIR_MANGROVE = wheelchair("mangrove");
    public static final Item WHEELCHAIR_CHERRY = wheelchair("cherry");
    public static final Item WHEELCHAIR_BAMBOO = wheelchair("bamboo");
    
    public static final Item WHEEL_OAK = wheel("oak");
    public static final Item WHEEL_SPRUCE = wheel("spruce");
    public static final Item WHEEL_BIRCH = wheel("birch");
    public static final Item WHEEL_DARK_OAK = wheel("dark_oak");
    public static final Item WHEEL_ACACIA = wheel("acacia");
    public static final Item WHEEL_JUNGLE = wheel("jungle");
    public static final Item WHEEL_CRIMSON = wheel("crimson");
    public static final Item WHEEL_WARPED = wheel("warped");
    public static final Item WHEEL_MANGROVE = wheel("mangrove");
    public static final Item WHEEL_CHERRY = wheel("cherry");
    public static final Item WHEEL_BAMBOO = wheel("bamboo");
    
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
    
    public static final Item CONTROLLER = register("controller", new OnAStickItem<EntityWheelchair>(new Item.Settings().arch$tab(WHEELCHAIR_TAB), WHCEntityTypes.WHEELCHAIR.get(), 0));
    
    private static Item register(String nameIn, Item itemIn)
    {
    	if(itemIn instanceof ItemWheelchair)
    		WHCBlocks.registerFakeBlock(nameIn);
    	ITEMS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), () -> itemIn);
    	return itemIn;
    }
    
    public static void init()
    {
    	ITEMS.register();
		TABS.register();
    }
    
    private static Item wheelchair(String name)
    {
    	String fullName = name+"_wheelchair";
    	return register(fullName, new ItemWheelchair(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(1)));
    }
    
    private static Item wheel(String name)
    {
    	return register(name+"_wheel", new Item(new Item.Settings().arch$tab(WHEELCHAIR_TAB).maxCount(2)));
    }
}
