package com.lying.init;

import java.util.function.Supplier;

import com.lying.data.recipe.RecipeWheelchair;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class WHCSpecialRecipes
{
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
	public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_TYPE);
	
	public static final RegistrySupplier<RecipeSerializer<RecipeWheelchair>> WHEELCHAIR_SERIALIZER = makeSerializer(RecipeWheelchair.ID, () -> new RecipeWheelchair.Serializer());
	
	static <T extends Recipe<?>> RegistrySupplier<RecipeSerializer<T>> makeSerializer(Identifier name, Supplier<RecipeSerializer<T>> serializer)
	{
		return SERIALIZERS.register(name, serializer);
	}
	
	@SuppressWarnings("unused")
	private static <T extends Recipe<?>> RegistrySupplier<RecipeType<T>> makeType(String name)
	{
		return TYPES.register(new Identifier(Reference.ModInfo.MOD_ID, name), () -> new RecipeType<T>() { public String toString() { return name; } });
	}
	
	public static void init()
	{
		SERIALIZERS.register();
		TYPES.register();
	}
}
