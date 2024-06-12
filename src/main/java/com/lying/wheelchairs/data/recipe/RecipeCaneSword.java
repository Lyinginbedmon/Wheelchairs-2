package com.lying.wheelchairs.data.recipe;

import java.util.function.Predicate;

import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.init.WHCEnchantments;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCSpecialRecipes;
import com.lying.wheelchairs.item.ItemCane;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeCaneSword extends SpecialCraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "cane_sword");
	
	private static final Predicate<ItemStack> IS_HOLLOW_CANE = stack -> 
	{
		if(!(stack.getItem() instanceof ItemCane))
			return false;
		
		if(!ItemCane.getSword(stack).isEmpty())
			return false;
		
		if(EnchantmentHelper.getLevel(WHCEnchantments.HOLLOWED, stack) <= 0)
			return false;
		return true;
	};
	
	public RecipeCaneSword()
	{
		super(CraftingRecipeCategory.MISC);
	}
	
	public boolean matches(RecipeInputInventory inv, World world)
	{
		ItemStack cane = ItemStack.EMPTY, sword = ItemStack.EMPTY;
		for(int slot=0; slot<inv.size(); slot++)
		{
			ItemStack stackInSlot = inv.getStack(slot);
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
	
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2)
	{
		ItemStack cane = ItemStack.EMPTY, sword = ItemStack.EMPTY;
		
		for(int slot=0; slot<inv.size(); slot++)
		{
			ItemStack stackInSlot = inv.getStack(slot);
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
		return WHCItems.CANE_OAK.getDefaultStack();
	}
	
	public static boolean isAppropriateSword(ItemStack stack)
	{
		return Wheelchairs.config.swordCaneFilter().test(stack);
	}
	
	public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.CANE_SWORD_SERIALIZER; }
}
