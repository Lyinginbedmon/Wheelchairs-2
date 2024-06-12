package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.wheelchairs.data.recipe.RecipeCane;
import com.lying.wheelchairs.data.recipe.RecipeCaneSword;
import com.lying.wheelchairs.data.recipe.RecipeHandle;
import com.lying.wheelchairs.data.recipe.RecipeWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WHCSpecialRecipes
{
	private static final Map<RecipeSerializer<?>, Identifier> RECIPE_SERIALIZERS = new HashMap<>();
	private static final Map<RecipeType<?>, Identifier> RECIPE_TYPES = new HashMap<>();
	
	public static final RecipeType<RecipeHandle> HANDLE_TYPE = makeType("handle");
	
	public static final RecipeSerializer<RecipeWheelchair> WHEELCHAIR_SERIALIZER = makeSerializer(RecipeWheelchair.ID, new RecipeWheelchair.Serializer());
	public static final RecipeSerializer<RecipeCane> CANE_SERIALIZER = makeSerializer(RecipeCane.ID, new RecipeCane.Serializer());
	public static final RecipeSerializer<RecipeHandle> HANDLE_SERIALIZER = makeSerializer(RecipeHandle.ID, new RecipeHandle.Serializer());
	public static final RecipeSerializer<RecipeCaneSword> CANE_SWORD_SERIALIZER = makeSerializer(RecipeCaneSword.ID, new SpecialRecipeSerializer<RecipeCaneSword>((category) -> new RecipeCaneSword()));
	
	static <T extends Recipe<?>> RecipeSerializer<T> makeSerializer(Identifier name, RecipeSerializer<T> serializer)
	{
		RECIPE_SERIALIZERS.put(serializer, name);
		return serializer;
	}
	
	private static <T extends Recipe<?>> RecipeType<T> makeType(String name)
	{
		RecipeType<T> type = new RecipeType<>() { public String toString() { return name; } };
		RECIPE_TYPES.put(type, new Identifier(Reference.ModInfo.MOD_ID, name));
		return type;
	}
	
	public static void init()
	{
		RECIPE_SERIALIZERS.keySet().forEach(serializer -> Registry.register(Registries.RECIPE_SERIALIZER, RECIPE_SERIALIZERS.get(serializer), serializer));
		RECIPE_TYPES.keySet().forEach(type -> Registry.register(Registries.RECIPE_TYPE, RECIPE_TYPES.get(type), type));
	}
}
