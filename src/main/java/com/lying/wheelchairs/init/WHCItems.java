package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.item.IBonusBlockItem;
import com.lying.wheelchairs.item.ItemCane;
import com.lying.wheelchairs.item.ItemCaneHandle;
import com.lying.wheelchairs.item.ItemCrutch;
import com.lying.wheelchairs.item.ItemWalker;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.OnAStickItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WHCItems
{
    private static final Map<Identifier, Item> ITEMS = new HashMap<>();
    
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
    
    public static final Item WALKER_OAK = walker("oak");
    public static final Item WALKER_SPRUCE = walker("spruce");
    public static final Item WALKER_BIRCH = walker("birch");
    public static final Item WALKER_DARK_OAK = walker("dark_oak");
    public static final Item WALKER_ACACIA = walker("acacia");
    public static final Item WALKER_JUNGLE = walker("jungle");
    public static final Item WALKER_CRIMSON = walker("crimson");
    public static final Item WALKER_WARPED = walker("warped");
    public static final Item WALKER_MANGROVE = walker("mangrove");
    public static final Item WALKER_CHERRY = walker("cherry");
    public static final Item WALKER_BAMBOO = walker("bamboo");
    
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
     * AAC speech device? Would require a Lot of sound, UI, and texture work
     * 
     * Service animal vests! (req. Cardinal Components)
     * 	NOTE Should store animal in Chairspace if it would drop below 1HP
     */
    
    public static final Item CRUTCH_OAK = crutch("oak");
    public static final Item CRUTCH_SPRUCE = crutch("spruce");
    public static final Item CRUTCH_BIRCH = crutch("birch");
    public static final Item CRUTCH_DARK_OAK = crutch("dark_oak");
    public static final Item CRUTCH_ACACIA = crutch("acacia");
    public static final Item CRUTCH_JUNGLE = crutch("jungle");
    public static final Item CRUTCH_CRIMSON = crutch("crimson");
    public static final Item CRUTCH_WARPED = crutch("warped");
    public static final Item CRUTCH_MANGROVE = crutch("mangrove");
    public static final Item CRUTCH_CHERRY = crutch("cherry");
    public static final Item CRUTCH_BAMBOO = crutch("bamboo");
    
    public static final Item CANE_OAK = cane("oak");
    public static final Item CANE_SPRUCE = cane("spruce");
    public static final Item CANE_BIRCH = cane("birch");
    public static final Item CANE_DARK_OAK = cane("dark_oak");
    public static final Item CANE_ACACIA = cane("acacia");
    public static final Item CANE_JUNGLE = cane("jungle");
    public static final Item CANE_CRIMSON = cane("crimson");
    public static final Item CANE_WARPED = cane("warped");
    public static final Item CANE_MANGROVE = cane("mangrove");
    public static final Item CANE_CHERRY = cane("cherry");
    public static final Item CANE_BAMBOO = cane("bamboo");
    
    public static final Item HANDLE_IRON = handle("iron");
    public static final Item HANDLE_GOLD = handle("gold");
    public static final Item HANDLE_SKULL = handle("skull");
    public static final Item HANDLE_WITHER = handle("wither_skull");
    public static final Item HANDLE_BONE = handle("bone");
    public static final Item HANDLE_OAK = handle("oak");
    public static final Item HANDLE_SPRUCE = handle("spruce");
    public static final Item HANDLE_BIRCH = handle("birch");
    public static final Item HANDLE_DARK_OAK = handle("dark_oak");
    public static final Item HANDLE_ACACIA = handle("acacia");
    public static final Item HANDLE_JUNGLE = handle("jungle");
    public static final Item HANDLE_CRIMSON = handle("crimson");
    public static final Item HANDLE_WARPED = handle("warped");
    public static final Item HANDLE_MANGROVE = handle("mangrove");
    public static final Item HANDLE_CHERRY = handle("cherry");
    public static final Item HANDLE_BAMBOO = handle("bamboo");
    
    public static final Item CONTROLLER = register("controller", new OnAStickItem<EntityWheelchair>(new FabricItemSettings(), WHCEntityTypes.WHEELCHAIR, 0));
    
    public static final ItemGroup WHEELCHAIR_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(WHEELCHAIR_OAK)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
	    {
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_OAK, WHEEL_OAK));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_SPRUCE, WHEEL_SPRUCE));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_BIRCH, WHEEL_BIRCH));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_DARK_OAK, WHEEL_DARK_OAK));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_JUNGLE, WHEEL_JUNGLE));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_ACACIA, WHEEL_ACACIA));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_CRIMSON, WHEEL_CRIMSON));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_WARPED, WHEEL_WARPED));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_MANGROVE, WHEEL_MANGROVE));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_CHERRY, WHEEL_CHERRY));
			entries.add(ItemWheelchair.withWheels(WHEELCHAIR_BAMBOO, WHEEL_BAMBOO));
			
			entries.add(ItemWalker.withWheels(WALKER_OAK, WHEEL_OAK));
			entries.add(ItemWalker.withWheels(WALKER_SPRUCE, WHEEL_SPRUCE));
			entries.add(ItemWalker.withWheels(WALKER_BIRCH, WHEEL_BIRCH));
			entries.add(ItemWalker.withWheels(WALKER_DARK_OAK, WHEEL_DARK_OAK));
			entries.add(ItemWalker.withWheels(WALKER_JUNGLE, WHEEL_JUNGLE));
			entries.add(ItemWalker.withWheels(WALKER_ACACIA, WHEEL_ACACIA));
			entries.add(ItemWalker.withWheels(WALKER_CRIMSON, WHEEL_CRIMSON));
			entries.add(ItemWalker.withWheels(WALKER_WARPED, WHEEL_WARPED));
			entries.add(ItemWalker.withWheels(WALKER_MANGROVE, WHEEL_MANGROVE));
			entries.add(ItemWalker.withWheels(WALKER_CHERRY, WHEEL_CHERRY));
			entries.add(ItemWalker.withWheels(WALKER_BAMBOO, WHEEL_BAMBOO));
			
			entries.add(WHEEL_OAK);
			entries.add(WHEEL_SPRUCE);
			entries.add(WHEEL_BIRCH);
			entries.add(WHEEL_DARK_OAK);
			entries.add(WHEEL_JUNGLE);
			entries.add(WHEEL_ACACIA);
			entries.add(WHEEL_CRIMSON);
			entries.add(WHEEL_WARPED);
			entries.add(WHEEL_MANGROVE);
			entries.add(WHEEL_CHERRY);
			entries.add(WHEEL_BAMBOO);
			
			entries.add(CRUTCH_OAK);
			entries.add(CRUTCH_SPRUCE);
			entries.add(CRUTCH_BIRCH);
			entries.add(CRUTCH_DARK_OAK);
			entries.add(CRUTCH_JUNGLE);
			entries.add(CRUTCH_ACACIA);
			entries.add(CRUTCH_CRIMSON);
			entries.add(CRUTCH_WARPED);
			entries.add(CRUTCH_MANGROVE);
			entries.add(CRUTCH_CHERRY);
			entries.add(CRUTCH_BAMBOO);
			
			entries.add(ItemCane.withHandle(CANE_OAK, HANDLE_OAK));
			entries.add(ItemCane.withHandle(CANE_SPRUCE, HANDLE_SPRUCE));
			entries.add(ItemCane.withHandle(CANE_BIRCH, HANDLE_BIRCH));
			entries.add(ItemCane.withHandle(CANE_DARK_OAK, HANDLE_DARK_OAK));
			entries.add(ItemCane.withHandle(CANE_JUNGLE, HANDLE_JUNGLE));
			entries.add(ItemCane.withHandle(CANE_ACACIA, HANDLE_ACACIA));
			entries.add(ItemCane.withHandle(CANE_CRIMSON, HANDLE_CRIMSON));
			entries.add(ItemCane.withHandle(CANE_WARPED, HANDLE_WARPED));
			entries.add(ItemCane.withHandle(CANE_MANGROVE, HANDLE_MANGROVE));
			entries.add(ItemCane.withHandle(CANE_CHERRY, HANDLE_CHERRY));
			entries.add(ItemCane.withHandle(CANE_BAMBOO, HANDLE_BAMBOO));
			
			entries.add(CONTROLLER);
	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	if(itemIn instanceof IBonusBlockItem)
    		WHCBlocks.registerFakeBlock(nameIn);
    	ITEMS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
    	return itemIn;
    }
    
    public static void init()
    {
		for(Entry<Identifier, Item> entry : ITEMS.entrySet())
			Registry.register(Registries.ITEM, entry.getKey(), entry.getValue());
		
		Registry.register(Registries.ITEM_GROUP, new Identifier(Reference.ModInfo.MOD_ID, "item_group"), WHEELCHAIR_GROUP);
    }
    
    private static Item wheelchair(String name)
    {
    	String fullName = name+"_wheelchair";
    	return register(fullName, new ItemWheelchair(new FabricItemSettings().maxCount(1)));
    }
    
    private static Item walker(String name)
    {
    	String fullName = name+"_walker";
    	return register(fullName, new ItemWalker(new FabricItemSettings().maxCount(1)));
    }
    
    private static Item wheel(String name)
    {
    	return register(name+"_wheel", new Item(new FabricItemSettings().maxCount(2)));
    }
    
    private static Item crutch(String name)
    {
    	return register(name+"_crutch", new ItemCrutch(new FabricItemSettings().maxCount(1)));
    }
    
    private static Item cane(String name)
    {
    	return register(name+"_cane", new ItemCane(new FabricItemSettings().maxCount(1)));
    }
    
    private static Item handle(String name)
    {
    	Item handle = register(name+"_handle", new ItemCaneHandle(new FabricItemSettings().maxCount(1)));
    	return handle;
    }
}
