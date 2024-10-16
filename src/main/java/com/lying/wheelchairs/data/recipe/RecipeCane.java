package com.lying.wheelchairs.data.recipe;

import java.util.Optional;

import com.lying.wheelchairs.init.WHCSpecialRecipes;
import com.lying.wheelchairs.item.ItemCane;
import com.lying.wheelchairs.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeManager.MatchGetter;
import net.minecraft.recipe.RecipeSerializer;
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
public class RecipeCane implements CraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "cane");
	
	private final ItemStack result;
	private final Ingredient staff;
	private static final Ingredient STICK = Ingredient.ofItems(Items.STICK);
	
	private final MatchGetter<Inventory, RecipeHandle> handleMatcher;
	private World currentWorld;
	
	public RecipeCane(ItemStack result, Ingredient staff)
	{
		this.result = result;
		this.staff = staff;
		this.handleMatcher = RecipeManager.createCachedMatchGetter(WHCSpecialRecipes.HANDLE_TYPE);
	}
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public boolean fits(int width, int height) { return width >= 1 && height >= 3; }
	
	public boolean matches(RecipeInputInventory inv, World world)
	{
		this.currentWorld = world;
		RecipeHandle handle = null;
		ItemStack staff = ItemStack.EMPTY, stick = ItemStack.EMPTY;
		for(int y=0; y<(inv.getHeight() - 2); y++)
			for(int x=0; x<inv.getWidth(); x++)
			{
				ItemStack handleMat = inv.getStack(coordsToIndex(x, y, inv.getWidth()));
				Optional<RecipeEntry<RecipeHandle>> handleRecipe = handleFromItem(handleMat, world);
				if(handleRecipe.isEmpty())
					continue;
				else
					handle = handleRecipe.get().value();
				
				ItemStack staffMat = inv.getStack(coordsToIndex(x, y + 1, inv.getWidth()));
				if(!this.staff.test(staffMat))
					continue;
				else
					staff = staffMat.copy();
				
				ItemStack stickMat = inv.getStack(coordsToIndex(x, y + 2, inv.getWidth()));
				if(!STICK.test(stickMat))
					continue;
				else
					stick = stickMat.copy();
			}
		return handle != null && !staff.isEmpty() && !stick.isEmpty();
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2)
	{
		ItemStack handle = ItemStack.EMPTY;
		
		for(int i=0; i<inv.size(); i++)
		{
			Optional<RecipeEntry<RecipeHandle>> handleRecipe = handleFromItem(inv.getStack(i), currentWorld);
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
		SimpleInventory inv = new SimpleInventory(1);
		inv.setStack(0, stack.copy());
		return handleMatcher.getFirstMatch(inv, world);
	}
	
	private static int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.CANE_SERIALIZER; }
	
    public static class Serializer implements RecipeSerializer<RecipeCane>
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
		private static final Codec<RecipeCane> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			((MapCodec)ItemStack.RECIPE_RESULT_CODEC.fieldOf("result")).forGetter(recipe -> ((RecipeCane)recipe).result),
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("staff")).forGetter(recipe -> ((RecipeCane)recipe).staff))
				.apply(instance, (a,b) -> new RecipeCane((ItemStack)a, (Ingredient)b)));
        
        public Codec<RecipeCane> codec() { return CODEC; }
        
        public RecipeCane read(PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient backing = Ingredient.fromPacket(packetByteBuf);
            return new RecipeCane(result, backing);
        }
        
        public void write(PacketByteBuf packetByteBuf, RecipeCane caneRecipe)
        {
        	packetByteBuf.writeItemStack(caneRecipe.result);
        	caneRecipe.staff.write(packetByteBuf);
        }
    }
}
