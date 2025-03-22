package com.lying.data.recipe;

import java.util.function.Predicate;

import com.lying.Wheelchairs;
import com.lying.init.WHCEnchantmentComponentTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCSpecialRecipes;
import com.lying.item.ItemCane;
import com.lying.reference.Reference;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeCaneSword extends SpecialCraftingRecipe
{
	public static final Identifier ID = Reference.ModInfo.prefix("cane_sword");
	
	private static final Predicate<ItemStack> IS_HOLLOW_CANE = stack -> 
	{
		return 
				stack.getItem() instanceof ItemCane && 
				EnchantmentHelper.hasAnyEnchantmentsWith(stack, WHCEnchantmentComponentTypes.CANE_INVENTORY.get()) && 
				ItemCane.getSword(stack).isEmpty();
	};
	
	public RecipeCaneSword()
	{
		super(CraftingRecipeCategory.MISC);
	}
	
	public boolean matches(CraftingRecipeInput inv, World world)
	{
		ItemStack cane = ItemStack.EMPTY, sword = ItemStack.EMPTY;
		for(int slot=0; slot<inv.size(); slot++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(slot);
			if(stackInSlot.isEmpty()) continue;
			
			if(IS_HOLLOW_CANE.test(stackInSlot))
			{
				if(cane.isEmpty())
					cane = stackInSlot.copy();
				else
					return false;
			}
			else if(isAppropriateSword(stackInSlot))
			{
				if(sword.isEmpty())
					sword = stackInSlot.copy();
				else
					return false;
			}
			else
				return false;
		}
		
		return !(cane.isEmpty() || sword.isEmpty());
	}
	
	public ItemStack craft(CraftingRecipeInput inv, RegistryWrapper.WrapperLookup var2)
	{
		ItemStack cane = ItemStack.EMPTY, sword = ItemStack.EMPTY;
		
		for(int slot=0; slot<inv.size(); slot++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(slot);
			if(stackInSlot.isEmpty()) continue;
			
			if(IS_HOLLOW_CANE.test(stackInSlot))
			{
				if(cane.isEmpty())
					cane = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
			else if(isAppropriateSword(stackInSlot))
			{
				if(sword.isEmpty())
					sword = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
			else
				return ItemStack.EMPTY;
		}
		
		if(!cane.isEmpty() && !sword.isEmpty())
			return ItemCane.setSword(cane, sword);
		else
			return ItemStack.EMPTY;
	}
	
	public boolean fits(int width, int height) { return width * height >= 2; }
	
	public ItemStack getResult(DynamicRegistryManager var1)
	{
		return WHCItems.CANE_OAK.get().getDefaultStack();
	}
	
	public static boolean isAppropriateSword(ItemStack stack)
	{
		return Wheelchairs.config.swordCaneFilter().test(stack);
	}
	
	public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() { return WHCSpecialRecipes.CANE_SWORD_SERIALIZER.get(); }
}
