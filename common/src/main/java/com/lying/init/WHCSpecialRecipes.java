package com.lying.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.data.recipe.RecipeWheelchair;
import com.lying.reference.Reference;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WHCSpecialRecipes
{
	private static final Map<RecipeSerializer<?>, Identifier> RECIPE_SERIALIZERS = new HashMap<>();
	private static final Map<RecipeType<?>, Identifier> RECIPE_TYPES = new HashMap<>();
	
	public static final RecipeSerializer<RecipeWheelchair> WHEELCHAIR_SERIALIZER = makeSerializer(RecipeWheelchair.ID, new RecipeWheelchair.Serializer());
	
	static <T extends Recipe<?>> RecipeSerializer<T> makeSerializer(Identifier name, RecipeSerializer<T> serializer)
	{
		RECIPE_SERIALIZERS.put(serializer, name);
		return serializer;
	}
	
	@SuppressWarnings("unused")
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
