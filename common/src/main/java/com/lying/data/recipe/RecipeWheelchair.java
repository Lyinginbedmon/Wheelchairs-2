package com.lying.data.recipe;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemWheelchair;
import com.lying.reference.Reference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
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
	public static final Identifier ID = Reference.ModInfo.prefix("wheelchair");
	
	private final ItemStack result;
	private final Ingredient backing, cushion, wheelLeft, wheelRight;
	
	private IngredientPlacement placement = null;
	
	public RecipeWheelchair(ItemStack result, Ingredient backing, Ingredient cushion, Optional<Ingredient> wheelL, Optional<Ingredient> wheelR)
	{
		this.result = result;
		this.backing = backing;
		this.cushion = cushion;
		this.wheelLeft = wheelL.get();
		this.wheelRight = wheelR.get();
	}
	
	public RecipeWheelchair(ItemStack result, Ingredient backing, Ingredient cushion, Ingredient wheelL, Ingredient wheelR)
	{
		this.result = result;
		this.backing = backing;
		this.cushion = cushion;
		this.wheelLeft = wheelL;
		this.wheelRight = wheelR;
	}
	
	public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }
	
	public IngredientPlacement getIngredientPlacement()
	{
		if(placement == null)
			placement = IngredientPlacement.forMultipleSlots(List.of(
					Optional.empty(), Optional.of(backing), Optional.empty(),
					Optional.of(wheelLeft), Optional.of(cushion), Optional.of(wheelRight)));
		return placement;
	}
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 2; }
	
	public boolean matches(CraftingRecipeInput inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
				if(checkFrom(inv, x, y) != null)
					return true;
		return false;
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(CraftingRecipeInput inv, RegistryWrapper.WrapperLookup var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
			{
				DefaultedList<ItemStack> contents = checkFrom(inv, x, y);
				if(contents == null)
					continue;
				
				ItemStack chair = this.result.copy();
				ItemWheelchair.setWheels(chair, contents.get(2), contents.get(3));
				
				ItemStack dye = contents.get(1);
				int colour = 0xF9FFFE;
				if(dye.getItem() instanceof BlockItem)
					colour = ((BlockItem)dye.getItem()).getBlock().getDefaultMapColor().color;
				else if(dye.getItem() instanceof DyeItem)
					colour = ((DyeItem)dye.getItem()).getColor().getMapColor().color;
				else if(dye.contains(DataComponentTypes.DYED_COLOR))
					colour = dye.get(DataComponentTypes.DYED_COLOR).rgb();
				chair.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(colour, true));
				
				return chair;
			}
		
		return ItemStack.EMPTY;
	}
	
	/**
	 * Returns the input crafting ingredients in a predetermined order<br>
	 *  * 0 - Backing material<br>
	 *  * 1 - Cushion material<br>
	 *  * 2 - Left wheel<br>
	 *  * 3 - Right wheel<br>
	 * Or returns null if the ingredients are not fully satisfed
	 */
	@Nullable
	private DefaultedList<ItemStack> checkFrom(CraftingRecipeInput inv, int x, int y)
	{
		DefaultedList<ItemStack> entries = DefaultedList.ofSize(4, ItemStack.EMPTY);
		
		int emptyA = coordsToIndex(x, y, inv.getWidth());
		int emptyB = coordsToIndex(x + 2, y, inv.getWidth());;
		if(!inv.getStackInSlot(emptyA).isEmpty() || !inv.getStackInSlot(emptyB).isEmpty())
			return null;
		
		int backing = coordsToIndex(x + 1, y, inv.getWidth());
		if(this.backing.test(inv.getStackInSlot(backing)))
			entries.set(0, inv.getStackInSlot(backing));
		else
			return null;
		
		int cushion = coordsToIndex(x + 1, y + 1, inv.getWidth());
		if(this.cushion.test(inv.getStackInSlot(cushion)))
			entries.set(1, inv.getStackInSlot(cushion));
		else
			return null;
		
		int leftWheel = coordsToIndex(x, y + 1, inv.getWidth());
		if(this.wheelLeft.test(inv.getStackInSlot(leftWheel)))
			entries.set(2, inv.getStackInSlot(leftWheel));
		else
			return null;
		int rightWheel = coordsToIndex(x + 2, y + 1, inv.getWidth());
		if(this.wheelRight.test(inv.getStackInSlot(rightWheel)))
			entries.set(3, inv.getStackInSlot(rightWheel));
		else
			return null;
		
		List<Integer> usedIndices = List.of(emptyA, backing, emptyB, leftWheel, cushion, rightWheel);
		for(int i=0; i<inv.size(); i++)
			if(!usedIndices.contains(i) && !inv.getStackInSlot(i).isEmpty())
				return null;
		
		return entries.stream().anyMatch(ItemStack::isEmpty) ? null : entries;
	}
	
	private int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() { return WHCSpecialRecipes.WHEELCHAIR_SERIALIZER.get(); }
	
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
        private static final MapCodec<RecipeWheelchair> CODEC	= RecordCodecBuilder.mapCodec(instance -> instance.group(
        		ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
        		Ingredient.CODEC.fieldOf("backing").forGetter(r -> r.backing),
        		Ingredient.CODEC.fieldOf("cushion").forGetter(r -> r.cushion),
        		Ingredient.CODEC.optionalFieldOf("left_wheel").forGetter(r -> Optional.of(r.wheelLeft)),
        		Ingredient.CODEC.optionalFieldOf("right_wheel").forGetter(r -> Optional.of(r.wheelRight))
        		).apply(instance, RecipeWheelchair::new));
        public static final PacketCodec<RegistryByteBuf, RecipeWheelchair> PACKET_CODEC	= PacketCodec.of((r, buf) -> 
        {
        	ItemStack.PACKET_CODEC.encode(buf, r.result);
        	Ingredient.PACKET_CODEC.encode(buf, r.backing);
        	Ingredient.PACKET_CODEC.encode(buf, r.cushion);
        	Ingredient.OPTIONAL_PACKET_CODEC.encode(buf, Optional.of(r.wheelLeft));
        	Ingredient.OPTIONAL_PACKET_CODEC.encode(buf, Optional.of(r.wheelRight));
        }, buf -> new RecipeWheelchair(ItemStack.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.PACKET_CODEC.decode(buf), Ingredient.OPTIONAL_PACKET_CODEC.decode(buf), Ingredient.OPTIONAL_PACKET_CODEC.decode(buf)));
        
        public MapCodec<RecipeWheelchair> codec() { return CODEC; }
        
		public PacketCodec<RegistryByteBuf, RecipeWheelchair> packetCodec() { return PACKET_CODEC; }
        
        
    }
}
