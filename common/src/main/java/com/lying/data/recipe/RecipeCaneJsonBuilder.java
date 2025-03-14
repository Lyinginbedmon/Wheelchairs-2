package com.lying.data.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RecipeCaneJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient backing;
	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
	
	public RecipeCaneJsonBuilder(ItemStack result, Ingredient backing, RecipeCategory category)
	{
		this.category = category;
		this.backing = backing;
		this.result = result;
	}
	
	public RecipeCaneJsonBuilder criterion(String name, AdvancementCriterion<?> criterion)
	{
		this.criteria.put(name, criterion);
		return this;
	}
	
	public void offerTo(RecipeExporter exporter, Identifier recipeId)
	{
		if(this.criteria.isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		RegistryKey<Recipe<?>> id = RegistryKey.of(RegistryKeys.RECIPE, recipeId);
		Advancement.Builder builder = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(id)).rewards(AdvancementRewards.Builder.recipe(id)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		this.criteria.forEach(builder::criterion);
		RecipeCane recipe = new RecipeCane(this.result, this.backing);
		exporter.accept(id, recipe, builder.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
	}
}
