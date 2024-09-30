package com.lying.init;

import java.util.function.Supplier;

import com.lying.Wheelchairs;
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
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class WHCSpecialRecipes
{
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
	public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.RECIPE_TYPE);
	
	public static final RegistrySupplier<RecipeType<RecipeHandle>> HANDLE_TYPE = makeType("handle");
	
	public static final RegistrySupplier<RecipeSerializer<RecipeWheelchair>> WHEELCHAIR_SERIALIZER = makeSerializer(RecipeWheelchair.ID, () -> new RecipeWheelchair.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeCane>> CANE_SERIALIZER = makeSerializer(RecipeCane.ID, () -> new RecipeCane.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeHandle>> HANDLE_SERIALIZER = makeSerializer(RecipeHandle.ID, () -> new RecipeHandle.Serializer());
	public static final RegistrySupplier<RecipeSerializer<RecipeCaneSword>> CANE_SWORD_SERIALIZER = makeSerializer(RecipeCaneSword.ID, () -> new SpecialRecipeSerializer<RecipeCaneSword>((id,cat) -> new RecipeCaneSword()));
	public static final RegistrySupplier<RecipeSerializer<RecipeWalker>> WALKER_SERIALIZER = makeSerializer(RecipeWalker.ID, () -> new RecipeWalker.Serializer());
	
	static <T extends Recipe<?>> RegistrySupplier<RecipeSerializer<T>> makeSerializer(Identifier name, Supplier<RecipeSerializer<T>> serializer)
	{
		return SERIALIZERS.register(name, serializer);
	}
	
	private static <T extends Recipe<?>> RegistrySupplier<RecipeType<T>> makeType(String name)
	{
		return TYPES.register(new Identifier(Reference.ModInfo.MOD_ID, name), () -> new RecipeType<T>() { public String toString() { return name; } });
	}
	
	public static void init()
	{
		SERIALIZERS.register();
		TYPES.register();
		Wheelchairs.LOGGER.info(" # Registered special recipes");
	}
}
