package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.item.ItemCrutch;
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
    
    public static final List<Item> WHEELCHAIRS = Lists.newArrayList();
    
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
     * 	NOTE Should be mainly plain items with specialised hand posing
     * 
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
			
			entries.add(CONTROLLER);
	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	if(itemIn instanceof ItemWheelchair)
    	{
    		WHEELCHAIRS.add(itemIn);
    		WHCBlocks.registerFakeBlock(nameIn);
    	}
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
    
    private static Item wheel(String name)
    {
    	return register(name+"_wheel", new Item(new FabricItemSettings().maxCount(2)));
    }
    
    private static Item crutch(String name)
    {
    	return register(name+"_crutch", new ItemCrutch(new FabricItemSettings().maxCount(1)));
    }
}
