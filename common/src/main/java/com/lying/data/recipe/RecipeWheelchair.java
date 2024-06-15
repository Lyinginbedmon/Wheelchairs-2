package com.lying.data.recipe;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lying.data.WHCItemTags;
import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemWheelchair;
import com.lying.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Defines a shaped recipe with the immutable arrangement of a 2x3 space with items occupying an upside-down T form.<br>
 * This structure then informs the NBT data applied to the result item.
 * @author Lying
 *
 */
public class RecipeWheelchair implements CraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "wheelchair");
	
	private final ItemStack result;
	private final Ingredient backing, cushion, wheelLeft, wheelRight;
	
	public RecipeWheelchair(ItemStack result, Ingredient backing, Ingredient cushion, Ingredient wheelL, Ingredient wheelR)
	{
		this.result = result;
		this.backing = backing;
		this.cushion = cushion;
		this.wheelLeft = wheelL;
		this.wheelRight = wheelR;
	}
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public Identifier getId() { return Registries.ITEM.getId(result.getItem()); }
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 2; }
	
	public boolean matches(RecipeInputInventory inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
				if(checkFrom(inv, x, y) != null)
					return true;
		return false;
	}
	
	public ItemStack getOutput(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
			{
				DefaultedList<ItemStack> contents = checkFrom(inv, x, y);
				if(contents == null)
					continue;
				
				ItemStack chair = this.result.copy();
				if(chair.getItem() instanceof ItemWheelchair)
					ItemWheelchair.setWheels(chair, contents.get(2), contents.get(3));
				if(chair.getItem() instanceof DyeableItem)
				{
					ItemStack dye = contents.get(1);
					int colour = 0xF9FFFE;
					if(dye.getItem() instanceof BlockItem)
						colour = ((BlockItem)dye.getItem()).getBlock().getDefaultMapColor().color;
					else if(dye.getItem() instanceof DyeItem)
						colour = componentsToColor(((DyeItem)dye.getItem()).getColor().getColorComponents());
					else if(dye.getItem() instanceof DyeableItem)
						colour = ((DyeableItem)dye.getItem()).getColor(dye);
					((DyeableItem)chair.getItem()).setColor(chair, colour);
				}
				
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
		
		int backing = coordsToIndex(x + 1, y, inv.getWidth());
		if(this.backing.test(inv.getStack(backing)))
			entries.set(0, inv.getStack(backing));
		else
			return null;
		
		int cushion = coordsToIndex(x + 1, y + 1, inv.getWidth());
		if(this.cushion.test(inv.getStack(cushion)))
			entries.set(1, inv.getStack(cushion));
		else
			return null;
		
		int leftWheel = coordsToIndex(x, y + 1, inv.getWidth());
		if(this.wheelLeft.test(inv.getStack(leftWheel)))
			entries.set(2, inv.getStack(leftWheel));
		else
			return null;
		int rightWheel = coordsToIndex(x + 2, y + 1, inv.getWidth());
		if(this.wheelRight.test(inv.getStack(rightWheel)))
			entries.set(3, inv.getStack(rightWheel));
		else
			return null;
		
		List<Integer> usedIndices = List.of(emptyA, backing, emptyB, leftWheel, cushion, rightWheel);
		for(int i=0; i<inv.size(); i++)
			if(!usedIndices.contains(i) && !inv.getStack(i).isEmpty())
				return null;
		
		return entries.stream().anyMatch(ItemStack::isEmpty) ? null : entries;
	}
	
	private int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.WHEELCHAIR_SERIALIZER.get(); }
	
	public static int componentsToColor(float[] comp)
	{
		int r = (int)(comp[0] * 255);
		int g = (int)(comp[1] * 255);
		int b = (int)(comp[2] * 255);
		
		// Recompose original decimal value of the dye colour from derived RGB values
		int col = r;
		col = (col << 8) + g;
		col = (col << 8) + b;
		
		return col;
	}
	
    public static class Serializer implements RecipeSerializer<RecipeWheelchair>
    {
    	public RecipeWheelchair read(Identifier recipeId, JsonObject json)
    	{
    		JsonObject item = json.get("result").getAsJsonObject();
    		ItemStack result = getItem(JsonHelper.getString(item, "item")).getDefaultStack().copy();
    		
    		Ingredient backing = Ingredient.fromJson(json.get("backing"));
    		
    		Ingredient cushion = json.has("cushion") ? Ingredient.fromJson(json.get("cushion")) : Ingredient.fromTag(ItemTags.WOOL);
    		Ingredient wheelL = json.has("left_wheel") ? Ingredient.fromJson(json.get("left_wheel")) : Ingredient.fromTag(WHCItemTags.WHEEL);
    		Ingredient wheelR = json.has("right_wheel") ? Ingredient.fromJson(json.get("right_wheel")) : Ingredient.fromTag(WHCItemTags.WHEEL);
    		
    		return new RecipeWheelchair(result, backing, cushion, wheelL, wheelR);
    	}
        
        public RecipeWheelchair read(Identifier recipeId, PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient backing = Ingredient.fromPacket(packetByteBuf);
            Ingredient cushion = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelL = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelR = Ingredient.fromPacket(packetByteBuf);
            return new RecipeWheelchair(result, backing, cushion, wheelL, wheelR);
        }
        
        public static Item getItem(String name)
        {
        	Identifier itemKey = new Identifier(name);
        	if(!Registries.ITEM.containsId(itemKey))
        		throw new JsonSyntaxException("Unknown item '" + name + "'");
        	
        	Item item = Registries.ITEM.get(itemKey);
        	if(item == Items.AIR)
        		throw new JsonSyntaxException("Invalid item: " + name);
        	return Objects.requireNonNull(item);
        }
        
        public void write(PacketByteBuf packetByteBuf, RecipeWheelchair wheelchairRecipe)
        {
        	packetByteBuf.writeItemStack(wheelchairRecipe.result);
        	wheelchairRecipe.backing.write(packetByteBuf);
        	wheelchairRecipe.cushion.write(packetByteBuf);
        	wheelchairRecipe.wheelLeft.write(packetByteBuf);
        	wheelchairRecipe.wheelRight.write(packetByteBuf);
        }
    }
}
