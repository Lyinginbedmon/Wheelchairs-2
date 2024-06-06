package com.lying.wheelchairs.data.recipe;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCSpecialRecipes;
import com.lying.wheelchairs.item.ItemCane;
import com.lying.wheelchairs.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
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
	
	public RecipeType<RecipeHandle> getType() { return WHCSpecialRecipes.HANDLE_TYPE; }
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
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
	public ItemStack getResult(DynamicRegistryManager var2)
	{
		return ItemCane.withHandle(WHCItems.CANE_OAK, this.result.getItem());
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
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.HANDLE_SERIALIZER; }
	
    public static class Serializer implements RecipeSerializer<RecipeHandle>
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
		private static final Codec<RecipeHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			((MapCodec)ItemStack.RECIPE_RESULT_CODEC.fieldOf("result")).forGetter(recipe -> ((RecipeHandle)recipe).result),
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("material")).forGetter(recipe -> ((RecipeHandle)recipe).material))
				.apply(instance, (a,b) -> new RecipeHandle((ItemStack)a, (Ingredient)b)));
        
        public Codec<RecipeHandle> codec() { return CODEC; }
        
        public RecipeHandle read(PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient backing = Ingredient.fromPacket(packetByteBuf);
            return new RecipeHandle(result, backing);
        }
        
        public void write(PacketByteBuf packetByteBuf, RecipeHandle handleRecipe)
        {
        	packetByteBuf.writeItemStack(handleRecipe.result);
        	handleRecipe.material.write(packetByteBuf);
        }
    }
}
