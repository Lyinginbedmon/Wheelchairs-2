package com.lying.wheelchairs.data.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class RecipeHandleJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient material;
	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
	
	public RecipeHandleJsonBuilder(ItemStack result, Ingredient backing, RecipeCategory category)
	{
		this.category = category;
		this.material = backing;
		this.result = result;
	}
	
	public RecipeHandleJsonBuilder criterion(String name, AdvancementCriterion<?> criterion)
	{
		this.criteria.put(name, criterion);
		return this;
	}
	
	public void offerTo(RecipeExporter exporter, Identifier recipeId)
	{
		if(this.criteria.isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		Advancement.Builder builder = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		this.criteria.forEach(builder::criterion);
		RecipeHandle recipe = new RecipeHandle(this.result, this.material);
		exporter.accept(recipeId, recipe, builder.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
	}
}
