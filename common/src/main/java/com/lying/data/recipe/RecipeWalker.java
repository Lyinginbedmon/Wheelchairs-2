package com.lying.data.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemWalker;
import com.lying.reference.Reference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Defines a shaped recipe with the immutable arrangement of a 2x3 space with items occupying an upside-down T form.<br>
 * This structure then informs the NBT data applied to the result item.
 * @author Lying
 *
 */
public class RecipeWalker implements CraftingRecipe
{
	public static final Identifier ID = Reference.ModInfo.prefix("walker");
	
	private final ItemStack result;
	private final Ingredient strut, platform, handle, wheelLeft, wheelRight;
	
	private final Map<Vector2i, Ingredient> recipeGrid = new HashMap<>();
	private IngredientPlacement placement = null;
	
	public RecipeWalker(ItemStack result, Ingredient strut, Ingredient platform, Ingredient handle, Ingredient wheelL, Ingredient wheelR)
	{
		this.result = result;
		this.strut = strut;
		this.platform = platform;
		this.handle = handle;
		this.wheelLeft = wheelL;
		this.wheelRight = wheelR;
		
		recipeGrid.put(new Vector2i(0,0), handle);
		recipeGrid.put(new Vector2i(2,0), handle);
		recipeGrid.put(new Vector2i(0,1), strut);
		recipeGrid.put(new Vector2i(1,1), platform);
		recipeGrid.put(new Vector2i(2,1), strut);
		recipeGrid.put(new Vector2i(0,2), wheelL);
		recipeGrid.put(new Vector2i(2,2), wheelR);
	}
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public IngredientPlacement getIngredientPlacement()
	{
		if(placement == null)
			placement = IngredientPlacement.forMultipleSlots(List.of(
					Optional.of(handle), Optional.empty(), Optional.of(handle), 
					Optional.of(strut), Optional.of(platform), Optional.of(strut),
					Optional.of(wheelLeft), Optional.empty(), Optional.of(wheelRight)));
		return placement;
	}
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 3; }
	
	public boolean matches(CraftingRecipeInput inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 2); y++)
				if(checkFrom(inv, x, y))
					return true;
		
		return false;
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(CraftingRecipeInput inv, WrapperLookup registries)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 2); y++)
				if(checkFrom(inv, x, y))
				{
					DefaultedList<ItemStack> components = getDataComps(inv, x, y);
					if(components == null)
						continue;
					
					ItemStack walker = this.result.copy();
					ItemWalker.setWheels(walker, components.get(0), components.get(1));
					return walker;
				}
		return ItemStack.EMPTY;
	}
	
	private boolean checkFrom(CraftingRecipeInput inv, int x, int y)
	{
		for(int i=0; i<3; i++)
			for(int j=0; j<3; j++)
			{
				ItemStack stackInSlot = inv.getStackInSlot(coordsToIndex(i+x, j+y, inv.getWidth()));
				Vector2i gridSlot = new Vector2i(i,j);
				
				if(recipeGrid.containsKey(gridSlot))
				{
					if(!recipeGrid.get(gridSlot).test(stackInSlot))
						return false;
				}
				else if(!stackInSlot.isEmpty())
					return false;
			}
		
		return true;
	}
	
	@Nullable
	private DefaultedList<ItemStack> getDataComps(CraftingRecipeInput inv, int x, int y)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, ItemStack.EMPTY);
		
		int leftWheelSlot = coordsToIndex(0 + x, 2 + y, inv.getWidth());
		int rightWheelSlot = coordsToIndex(2 + x, 2 + y, inv.getWidth());
		
		if(!inv.getStackInSlot(leftWheelSlot).isEmpty() && wheelLeft.test(inv.getStackInSlot(leftWheelSlot)))
			wheels.set(0, inv.getStackInSlot(leftWheelSlot));
		else
			return null;
		
		if(!inv.getStackInSlot(rightWheelSlot).isEmpty() && wheelLeft.test(inv.getStackInSlot(rightWheelSlot)))
			wheels.set(1, inv.getStackInSlot(rightWheelSlot));
		else
			return null;
		
		return wheels;
	}
	
	private int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() { return WHCSpecialRecipes.WALKER_SERIALIZER.get(); }
	
    public static class Serializer implements RecipeSerializer<RecipeWalker>
    {
		private static final MapCodec<RecipeWalker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
        	Ingredient.CODEC.fieldOf("strut").forGetter(r -> r.strut), 
        	Ingredient.CODEC.fieldOf("platform").forGetter(r -> r.platform), 
        	Ingredient.CODEC.fieldOf("handle").forGetter(r -> r.handle), 
        	Ingredient.CODEC.fieldOf("left_wheel").forGetter(r -> r.wheelLeft), 
        	Ingredient.CODEC.fieldOf("right_wheel").forGetter(r -> r.wheelRight)
        	).apply(instance, RecipeWalker::new));
        private static final PacketCodec<RegistryByteBuf, RecipeWalker> PACKET_CODEC	= PacketCodec.of((r, buf) -> 
        {
        	ItemStack.PACKET_CODEC.encode(buf, r.result);
        	Ingredient.PACKET_CODEC.encode(buf, r.strut);
        	Ingredient.PACKET_CODEC.encode(buf, r.platform);
        	Ingredient.PACKET_CODEC.encode(buf, r.handle);
        	Ingredient.PACKET_CODEC.encode(buf, r.wheelLeft);
        	Ingredient.PACKET_CODEC.encode(buf, r.wheelRight);
        }, buf -> new RecipeWalker(ItemStack.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf)));
        
        public MapCodec<RecipeWalker> codec() { return CODEC; }
        
		public PacketCodec<RegistryByteBuf, RecipeWalker> packetCodec() { return PACKET_CODEC; }
    }
}
