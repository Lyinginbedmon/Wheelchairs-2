package com.lying.data.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.data.WHCItemTags;
import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemWheelchair;
import com.lying.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
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
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 2; }
	
	public boolean matches(RecipeInputInventory inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 1); y++)
				if(checkFrom(inv, x, y) != null)
					return true;
		return false;
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
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
        @SuppressWarnings({ "unchecked", "rawtypes" })
		private static final Codec<RecipeWheelchair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			((MapCodec)ItemStack.RECIPE_RESULT_CODEC.fieldOf("result")).forGetter(recipe -> ((RecipeWheelchair)recipe).result),
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("backing")).forGetter(recipe -> ((RecipeWheelchair)recipe).backing), 
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("cushion")).orElse(Ingredient.fromTag(ItemTags.WOOL)).forGetter(recipe -> ((RecipeWheelchair)recipe).cushion), 
        	((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("left_wheel")).orElse(Ingredient.fromTag(WHCItemTags.WHEEL)).forGetter(recipe -> ((RecipeWheelchair)recipe).wheelLeft), 
        	((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("right_wheel")).orElse(Ingredient.fromTag(WHCItemTags.WHEEL)).forGetter(recipe -> ((RecipeWheelchair)recipe).wheelRight))
				.apply(instance, (a,b,c,d,e) -> new RecipeWheelchair((ItemStack)a, (Ingredient)b, (Ingredient)c, (Ingredient)d, (Ingredient)e)));
        
        public Codec<RecipeWheelchair> codec() { return CODEC; }
        
        public RecipeWheelchair read(PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient backing = Ingredient.fromPacket(packetByteBuf);
            Ingredient cushion = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelL = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelR = Ingredient.fromPacket(packetByteBuf);
            return new RecipeWheelchair(result, backing, cushion, wheelL, wheelR);
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
