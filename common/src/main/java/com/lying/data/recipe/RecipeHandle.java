package com.lying.data.recipe;

import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lying.init.WHCItems;
import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemCane;
import com.lying.reference.Reference;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

/**
 * Defines a shaped recipe with the immutable arrangement of a 2x3 space with items occupying an upside-down T form.<br>
 * This structure then informs the NBT data applied to the result item.
 * @author Lying
 *
 */
public class RecipeHandle implements Recipe<Inventory>
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "handle");
	
	private final ItemStack result;
	private final Ingredient material;
	
	public RecipeHandle(ItemStack result, Ingredient staff)
	{
		this.result = result;
		this.material = staff;
	}
	
	public Identifier getId() { return ID; }
	
	public RecipeType<RecipeHandle> getType() { return WHCSpecialRecipes.HANDLE_TYPE.get(); }
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public boolean isIgnoredInRecipeBook() { return true; }
	
	public boolean fits(int width, int height) { return width >= 1 && height >= 1; }
	
	public boolean matches(Inventory inv, World var2)
	{
		ItemStack mat = ItemStack.EMPTY;
		for(int i=0; i<inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getStack(i);
			if(material.test(stackInSlot))
			{
				if(mat.isEmpty())
					mat = stackInSlot.copy();
				else
					return false;
			}
		}
		
		return !mat.isEmpty();
	}
	
	/** Gets an oak cane with this handle */
	public ItemStack getOutput(DynamicRegistryManager var2)
	{
		return ItemCane.withHandle(WHCItems.CANE_OAK.get(), this.result.getItem());
	}
	
	/** Returns the actual item for this recipe that should be added to the cane */
	public ItemStack getResult() { return this.result.copy(); }
	
	public ItemStack craft(Inventory inv, DynamicRegistryManager var2)
	{
		ItemStack mat = ItemStack.EMPTY;
		
		for(int i=0; i<inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getStack(i);
			if(material.test(stackInSlot))
			{
				if(mat.isEmpty())
					mat = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
		}
		
		return !mat.isEmpty() ? this.result.copy() : ItemStack.EMPTY;
	}
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.HANDLE_SERIALIZER.get(); }
	
    public static class Serializer implements RecipeSerializer<RecipeHandle>
    {
        public RecipeHandle read(Identifier recipeId, JsonObject json)
        {
    		JsonObject item = json.get("result").getAsJsonObject();
    		ItemStack result = getItem(JsonHelper.getString(item, "item")).getDefaultStack().copy();
    		
    		Ingredient material = Ingredient.fromJson(json.get("material"));
    		return new RecipeHandle(result, material);
        }
        
        public RecipeHandle read(Identifier recipeId, PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient backing = Ingredient.fromPacket(packetByteBuf);
            return new RecipeHandle(result, backing);
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
        
        public void write(PacketByteBuf packetByteBuf, RecipeHandle handleRecipe)
        {
        	packetByteBuf.writeItemStack(handleRecipe.result);
        	handleRecipe.material.write(packetByteBuf);
        }
    }
}
