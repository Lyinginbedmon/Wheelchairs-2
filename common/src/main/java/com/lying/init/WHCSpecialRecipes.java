package com.lying.init;

import com.lying.data.recipe.RecipeCane;
import com.lying.data.recipe.RecipeCaneSword;
import com.lying.data.recipe.RecipeHandle;
import com.lying.data.recipe.RecipeWalker;
import com.lying.data.recipe.RecipeWheelchair;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class WHCSpecialRecipes
{
	private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_TYPE);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
	
	public static final RegistrySupplier<RecipeType<RecipeHandle>> HANDLE_TYPE = makeType("handle");
	
	public static final RegistrySupplier<RecipeSerializer<RecipeWheelchair>> WHEELCHAIR_SERIALIZER = makeSerializer(RecipeWheelchair.ID, new RecipeWheelchair.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeCane>> CANE_SERIALIZER = makeSerializer(RecipeCane.ID, new RecipeCane.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeHandle>> HANDLE_SERIALIZER = makeSerializer(RecipeHandle.ID, new RecipeHandle.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeCaneSword>> CANE_SWORD_SERIALIZER = makeSerializer(RecipeCaneSword.ID, new SpecialRecipeSerializer<RecipeCaneSword>((category) -> new RecipeCaneSword()));
	public static final RegistrySupplier<RecipeSerializer<RecipeWalker>> WALKER_SERIALIZER = makeSerializer(RecipeWalker.ID, new RecipeWalker.Serializer());
	
	private static <T extends Recipe<?>> RegistrySupplier<RecipeSerializer<T>> makeSerializer(Identifier name, RecipeSerializer<T> serializer)
	{
		return RECIPE_SERIALIZERS.register(name, () -> serializer);
	}
	
	private static <T extends Recipe<?>> RegistrySupplier<RecipeType<T>> makeType(String name)
	{
		return RECIPE_TYPES.register(Reference.ModInfo.prefix(name), () -> new RecipeType<T>() { public String toString() { return name; } });
	}
	
	public static void init()
	{
		RECIPE_TYPES.register();
		RECIPE_SERIALIZERS.register();
	}
}