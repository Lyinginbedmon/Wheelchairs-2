package com.lying.wheelchairs.data.recipe;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.init.WHCSpecialRecipes;
import com.lying.wheelchairs.item.ItemWalker;
import com.lying.wheelchairs.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
public class RecipeWalker implements CraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "walker");
	
	private final ItemStack result;
	private final Ingredient strut, platform, handle, wheelLeft, wheelRight;
	
	private final Map<Vector2i, Ingredient> recipeGrid = new HashMap<>();
	
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
	
	public boolean fits(int width, int height) { return width >= 3 && height >= 3; }
	
	public boolean matches(RecipeInputInventory inv, World var2)
	{
		for(int x=0; x<(inv.getWidth() - 2); x++)
			for(int y=0; y<(inv.getHeight() - 2); y++)
				if(checkFrom(inv, x, y))
					return true;
		
		return false;
	}
	
	public ItemStack getResult(DynamicRegistryManager var2) { return this.result.copy(); }
	
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2)
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
	
	private boolean checkFrom(RecipeInputInventory inv, int x, int y)
	{
		for(int i=0; i<3; i++)
			for(int j=0; j<3; j++)
			{
				ItemStack stackInSlot = inv.getStack(coordsToIndex(i+x, j+y, inv.getWidth()));
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
	private DefaultedList<ItemStack> getDataComps(RecipeInputInventory inv, int x, int y)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, ItemStack.EMPTY);
		
		int leftWheelSlot = coordsToIndex(0 + x, 2 + y, inv.getWidth());
		int rightWheelSlot = coordsToIndex(2 + x, 2 + y, inv.getWidth());
		
		if(!inv.getStack(leftWheelSlot).isEmpty() && wheelLeft.test(inv.getStack(leftWheelSlot)))
			wheels.set(0, inv.getStack(leftWheelSlot));
		else
			return null;
		
		if(!inv.getStack(rightWheelSlot).isEmpty() && wheelLeft.test(inv.getStack(rightWheelSlot)))
			wheels.set(1, inv.getStack(rightWheelSlot));
		else
			return null;
		
		return wheels;
	}
	
	private int coordsToIndex(int x, int y, int width) { return x + (y * width); }
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.WALKER_SERIALIZER; }
	
    public static class Serializer implements RecipeSerializer<RecipeWalker>
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
		private static final Codec<RecipeWalker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			((MapCodec)ItemStack.RECIPE_RESULT_CODEC.fieldOf("result")).forGetter(recipe -> ((RecipeWalker)recipe).result),
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("strut")).forGetter(recipe -> ((RecipeWalker)recipe).strut), 
        	((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("platform")).orElse(Ingredient.fromTag(ItemTags.WOOL)).forGetter(recipe -> ((RecipeWalker)recipe).platform), 
        	((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("handle")).orElse(Ingredient.ofItems(Items.STICK)).forGetter(recipe -> ((RecipeWalker)recipe).handle), 
        	((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("left_wheel")).orElse(Ingredient.fromTag(WHCItemTags.WHEEL)).forGetter(recipe -> ((RecipeWalker)recipe).wheelLeft), 
        	((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("right_wheel")).orElse(Ingredient.fromTag(WHCItemTags.WHEEL)).forGetter(recipe -> ((RecipeWalker)recipe).wheelRight))
				.apply(instance, (a,b,c,d,e, f) -> new RecipeWalker((ItemStack)a, (Ingredient)b, (Ingredient)c, (Ingredient)d, (Ingredient)e, (Ingredient)f)));
        
        public Codec<RecipeWalker> codec() { return CODEC; }
        
        public RecipeWalker read(PacketByteBuf packetByteBuf)
        {
        	ItemStack result = packetByteBuf.readItemStack();
            Ingredient strut = Ingredient.fromPacket(packetByteBuf);
            Ingredient platform = Ingredient.fromPacket(packetByteBuf);
            Ingredient handle = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelL = Ingredient.fromPacket(packetByteBuf);
            Ingredient wheelR = Ingredient.fromPacket(packetByteBuf);
            return new RecipeWalker(result, strut, platform, handle, wheelL, wheelR);
        }
        
        public void write(PacketByteBuf packetByteBuf, RecipeWalker walkerRecipe)
        {
        	packetByteBuf.writeItemStack(walkerRecipe.result);
        	walkerRecipe.strut.write(packetByteBuf);
        	walkerRecipe.platform.write(packetByteBuf);
        	walkerRecipe.handle.write(packetByteBuf);
        	walkerRecipe.wheelLeft.write(packetByteBuf);
        	walkerRecipe.wheelRight.write(packetByteBuf);
        }
    }
}
