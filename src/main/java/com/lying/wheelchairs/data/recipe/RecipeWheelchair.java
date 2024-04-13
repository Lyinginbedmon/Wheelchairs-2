package com.lying.wheelchairs.data.recipe;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCSpecialRecipes;
import com.lying.wheelchairs.item.ItemWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class RecipeWheelchair extends SpecialCraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "wheelchair");
	protected static final Map<Item, Item> BACKING_MAP = new HashMap<>();
	
	public RecipeWheelchair(CraftingRecipeCategory category) { super(CraftingRecipeCategory.MISC); }
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 2; }
	
	public boolean matches(RecipeInputInventory inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
				if(checkFrom(inv, x, y) != null)
					return true;
		return false;
	}
	
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
			{
				DefaultedList<ItemStack> contents = checkFrom(inv, x, y);
				if(contents == null)
					continue;
				
				ItemStack chair = BACKING_MAP.get(contents.get(0).getItem()).getDefaultStack().copy();
				ItemWheelchair.setWheels(chair, contents.get(2), contents.get(3));
				((DyeableItem)chair.getItem()).setColor(chair, ((BlockItem)contents.get(1).getItem()).getBlock().getDefaultMapColor().color);
				return chair;
			}
		
		return ItemStack.EMPTY;
	}
	
	@Nullable
	private DefaultedList<ItemStack> checkFrom(RecipeInputInventory inv, int x, int y)
	{
		DefaultedList<ItemStack> entries = DefaultedList.ofSize(4, ItemStack.EMPTY);
		
		int emptyA = coordsToIndex(x, y, inv.getWidth());
		int emptyB = coordsToIndex(x + 2, y, inv.getWidth());;
		if(!inv.getStack(emptyA).isEmpty() || !inv.getStack(emptyB).isEmpty())
			return null;
		
		int cushion = coordsToIndex(x + 1, y + 1, inv.getWidth());
		if(inv.getStack(cushion).isIn(ItemTags.WOOL) && inv.getStack(cushion).getItem() instanceof BlockItem)
			entries.set(1, inv.getStack(cushion));
		else
			return null;
		
		int leftWheel = coordsToIndex(x, y + 1, inv.getWidth());
		if(inv.getStack(leftWheel).isIn(WHCItemTags.WHEEL))
			entries.set(2, inv.getStack(leftWheel));
		else
			return null;
		int rightWheel = coordsToIndex(x + 2, y + 1, inv.getWidth());
		if(inv.getStack(rightWheel).isIn(WHCItemTags.WHEEL))
			entries.set(3, inv.getStack(rightWheel));
		else
			return null;
		
		int backing = coordsToIndex(x + 1, y, inv.getWidth());
		if(BACKING_MAP.containsKey(inv.getStack(backing).getItem()))
			entries.set(0, inv.getStack(backing));
		else
			return null;
		
		return entries.stream().anyMatch(ItemStack::isEmpty) ? null : entries;
	}
	
	private int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.WHEELCHAIR_SERIALIZER; }
	
	private static void mapChair(Item item1, Item item2) { BACKING_MAP.put(item1, item2); }
	
	static
	{
		mapChair(Items.OAK_LOG, WHCItems.WHEELCHAIR_OAK);
		mapChair(Items.SPRUCE_LOG, WHCItems.WHEELCHAIR_SPRUCE);
		mapChair(Items.BIRCH_LOG, WHCItems.WHEELCHAIR_BIRCH);
		mapChair(Items.DARK_OAK_LOG, WHCItems.WHEELCHAIR_DARK_OAK);
		mapChair(Items.JUNGLE_LOG, WHCItems.WHEELCHAIR_JUNGLE);
		mapChair(Items.ACACIA_LOG, WHCItems.WHEELCHAIR_ACACIA);
		mapChair(Items.CRIMSON_STEM, WHCItems.WHEELCHAIR_CRIMSON);
		mapChair(Items.WARPED_STEM, WHCItems.WHEELCHAIR_WARPED);
	}
}
