package com.lying.data.recipe;

import java.util.List;
import java.util.Optional;

import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemCane;
import com.lying.reference.Reference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
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
public class RecipeCane implements CraftingRecipe
{
	public static final Identifier ID = Reference.ModInfo.prefix("cane");
	
	private final ItemStack result;
	private final Ingredient staff;
	private static final Ingredient STICK = Ingredient.ofItems(Items.STICK);
	
	private World currentWorld;
	private IngredientPlacement placement;
	
	public RecipeCane(ItemStack result, Ingredient staff)
	{
		this.result = result;
		this.staff = staff;
	}
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public IngredientPlacement getIngredientPlacement()
	{
		if(placement == null)
			placement = IngredientPlacement.forMultipleSlots(List.of(
					Optional.of(staff), Optional.of(STICK)));
		return placement;
	}
	
	public boolean fits(int width, int height) { return width >= 1 && height >= 3; }
	
	public boolean matches(CraftingRecipeInput inv, World world)
	{
		this.currentWorld = world;
		RecipeHandle handle = null;
		ItemStack staff = ItemStack.EMPTY, stick = ItemStack.EMPTY;
		for(int y=0; y<(inv.getHeight() - 2); y++)
			for(int x=0; x<inv.getWidth(); x++)
			{
				ItemStack handleMat = inv.getStackInSlot(coordsToIndex(x, y, inv.getWidth()));
				Optional<RecipeEntry<RecipeHandle>> handleRecipe = handleFromItem(handleMat, world);
				if(handleRecipe.isEmpty())
					continue;
				else
					handle = handleRecipe.get().value();
				
				ItemStack staffMat = inv.getStackInSlot(coordsToIndex(x, y + 1, inv.getWidth()));
				if(!this.staff.test(staffMat))
					continue;
				else
					staff = staffMat.copy();
				
				ItemStack stickMat = inv.getStackInSlot(coordsToIndex(x, y + 2, inv.getWidth()));
				if(!STICK.test(stickMat))
					continue;
				else
					stick = stickMat.copy();
			}
		return handle != null && !staff.isEmpty() && !stick.isEmpty();
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(CraftingRecipeInput inv, RegistryWrapper.WrapperLookup var2)
	{
		ItemStack handle = ItemStack.EMPTY;
		
		for(int i=0; i<inv.size(); i++)
		{
			Optional<RecipeEntry<RecipeHandle>> handleRecipe = handleFromItem(inv.getStackInSlot(i), currentWorld);
			if(handleRecipe.isPresent())
			{
				handle = handleRecipe.get().value().getResult();
				break;
			}
		}
		
		if(!handle.isEmpty())
		{
			ItemStack stack = this.result.copy();
			ItemCane.setHandle(stack, handle);
			return stack;
		}
		else
			return ItemStack.EMPTY;
	}
	
	public Optional<RecipeEntry<RecipeHandle>> handleFromItem(ItemStack stack, World world)
	{
		ServerRecipeManager recipeManager = world.getServer().getRecipeManager();
		return recipeManager.getFirstMatch(WHCSpecialRecipes.HANDLE_TYPE.get(), new SingleStackRecipeInput(stack.copy()), world);
	}
	
	private static int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() { return WHCSpecialRecipes.CANE_SERIALIZER.get(); }
	
    public static class Serializer implements RecipeSerializer<RecipeCane>
    {
		private static final MapCodec<RecipeCane> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
        	Ingredient.CODEC.fieldOf("staff").forGetter(r -> r.staff))
				.apply(instance, RecipeCane::new));
        public static final PacketCodec<RegistryByteBuf, RecipeCane> PACKET_CODEC	= PacketCodec.of((r, buf) -> 
        {
        	ItemStack.PACKET_CODEC.encode(buf, r.result);
        	Ingredient.PACKET_CODEC.encode(buf, r.staff);
        }, buf -> new RecipeCane(ItemStack.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf)));
        
        public MapCodec<RecipeCane> codec() { return CODEC; }
        
        public PacketCodec<RegistryByteBuf, RecipeCane> packetCodec() { return PACKET_CODEC; }
    }
}
