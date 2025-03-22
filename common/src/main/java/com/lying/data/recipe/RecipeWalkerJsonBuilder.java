package com.lying.data.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

import com.lying.data.WHCTags;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RecipeWalkerJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient strut, platform, wheelL, wheelR, handle;
	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
	
	public RecipeWalkerJsonBuilder(ItemStack result, Ingredient strut, Ingredient platform, RecipeCategory category)
	{
		this(
				result, 
				strut, 
				platform, 
				Ingredient.ofItems(Items.STICK), 
				Ingredient.fromTag(Registries.ITEM.getOrThrow(WHCTags.WHEEL)), 
				Ingredient.fromTag(Registries.ITEM.getOrThrow(WHCTags.WHEEL)), category);
	}
	
	public RecipeWalkerJsonBuilder(ItemStack result, Ingredient strut, Ingredient platform, Ingredient handle, Ingredient wheelL, Ingredient wheelR, RecipeCategory category)
	{
		this.category = category;
		this.strut = strut;
		this.platform = platform;
		this.handle = handle;
		this.wheelL = wheelL;
		this.wheelR = wheelR;
		this.result = result;
	}
	
	public RecipeWalkerJsonBuilder criterion(String name, AdvancementCriterion<?> criterion)
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
		RecipeWalker recipe = new RecipeWalker(this.result, this.strut, this.platform, this.handle, this.wheelL, this.wheelR);
		exporter.accept(id, recipe, builder.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
	}
}
