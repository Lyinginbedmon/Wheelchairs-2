package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.wheelchairs.reference.Reference;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WHCItems
{
    private static final Map<Identifier, Item> ITEMS = new HashMap<>();
    
//    public static final Item PRESCIENCE_ITEM = register("bottle_prescience", new BlockItem(WHCBlocks.PRESCIENCE, new FabricItemSettings().rarity(Rarity.RARE)));
    
//    public static final ItemGroup WHEELCHAIR_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(SAGE_HAT)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
//	    {
//			entries.add(PRESCIENCE_ITEM);
//	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	ITEMS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
    	return itemIn;
    }
    
    public static void init()
    {
		for(Entry<Identifier, Item> entry : ITEMS.entrySet())
			Registry.register(Registries.ITEM, entry.getKey(), entry.getValue());
		
//		Registry.register(Registries.ITEM_GROUP, new Identifier(Reference.ModInfo.MOD_ID, "item_group"), WHEELCHAIR_GROUP);
    }
}
