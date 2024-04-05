package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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
    
    public static final Item WHEEL_OAK = wheel("oak");
    public static final Item WHEEL_SPRUCE = wheel("spruce");
    public static final Item WHEEL_BIRCH = wheel("birch");
    public static final Item WHEEL_DARK_OAK = wheel("dark_oak");
    public static final Item WHEEL_ACACIA = wheel("acacia");
    public static final Item WHEEL_JUNGLE = wheel("jungle");
    public static final Item WHEEL_CRIMSON = wheel("crimson");
    public static final Item WHEEL_WARPED = wheel("warped");
    public static final Item WHEEL_MANGROVE = wheel("mangrove");
    
    public static final ItemGroup WHEELCHAIR_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(WHEELCHAIR_OAK)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
	    {
			entries.add(WHEELCHAIR_OAK);
			entries.add(WHEELCHAIR_SPRUCE);
			entries.add(WHEELCHAIR_BIRCH);
			entries.add(WHEELCHAIR_DARK_OAK);
			entries.add(WHEELCHAIR_JUNGLE);
			entries.add(WHEELCHAIR_ACACIA);
			entries.add(WHEELCHAIR_CRIMSON);
			entries.add(WHEELCHAIR_WARPED);
			
			entries.add(WHEEL_OAK);
			entries.add(WHEEL_SPRUCE);
			entries.add(WHEEL_BIRCH);
			entries.add(WHEEL_DARK_OAK);
			entries.add(WHEEL_JUNGLE);
			entries.add(WHEEL_ACACIA);
			entries.add(WHEEL_CRIMSON);
			entries.add(WHEEL_WARPED);
	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	if(itemIn instanceof ItemWheelchair)
    	{
    		WHEELCHAIRS.add(itemIn);
    		WHCBlocks.registerSeat(nameIn);
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
}
