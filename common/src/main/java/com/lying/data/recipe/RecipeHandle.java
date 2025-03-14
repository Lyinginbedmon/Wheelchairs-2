package com.lying.data.recipe;

import com.lying.init.WHCItems;
import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemCane;
import com.lying.reference.Reference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Defines a shaped recipe with the immutable arrangement of a 2x3 space with items occupying an upside-down T form.<br>
 * This structure then informs the NBT data applied to the result item.
 * @author Lying
 *
 */
public class RecipeHandle implements Recipe<RecipeInput>
{
	public static final Identifier ID = Reference.ModInfo.prefix("handle");
	
	private final ItemStack result;
	private final Ingredient material;
	
	public RecipeHandle(ItemStack result, Ingredient staff)
	{
		this.result = result;
		this.material = staff;
	}
	
	public RecipeType<RecipeHandle> getType() { return WHCSpecialRecipes.HANDLE_TYPE.get(); }
	
	public RecipeBookCategory getRecipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
	
	public IngredientPlacement getIngredientPlacement() { return IngredientPlacement.NONE; }	// XXX ????
	
	public boolean isIgnoredInRecipeBook() { return true; }
	
	public boolean fits(int width, int height) { return width >= 1 && height >= 1; }
	
	public boolean matches(RecipeInput inv, World var2)
	{
		ItemStack mat = ItemStack.EMPTY;
		for(int i=0; i<inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
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
		return ItemCane.withHandle(WHCItems.CANE_OAK.get(), this.result.getItem());
	}
	
	/** Returns the actual item for this recipe that should be added to the cane */
	public ItemStack getResult() { return this.result.copy(); }
	
	public ItemStack craft(RecipeInput inv, RegistryWrapper.WrapperLookup var2)
	{
		ItemStack mat = ItemStack.EMPTY;
		
		for(int i=0; i<inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
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
	
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() { return WHCSpecialRecipes.HANDLE_SERIALIZER.get(); }
	
    public static class Serializer implements RecipeSerializer<RecipeHandle>
    {
		private static final MapCodec<RecipeHandle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
        	Ingredient.CODEC.fieldOf("material").forGetter(r -> r.material))
				.apply(instance, RecipeHandle::new));
        public static final PacketCodec<RegistryByteBuf, RecipeHandle> PACKET_CODEC	= PacketCodec.of((r, buf) -> 
        {
        	ItemStack.PACKET_CODEC.encode(buf, r.result);
        	Ingredient.PACKET_CODEC.encode(buf, r.material);
        }, buf -> new RecipeHandle(ItemStack.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf)));
        
        public MapCodec<RecipeHandle> codec() { return CODEC; }
        
        public PacketCodec<RegistryByteBuf, RecipeHandle> packetCodec() { return PACKET_CODEC; }
    }
}
