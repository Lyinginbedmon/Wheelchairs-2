package com.lying.data.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.lying.init.WHCSpecialRecipes;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class RecipeHandleJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient material;
	private final Advancement.Builder criteria = Advancement.Builder.createUntelemetered();
	
	public RecipeHandleJsonBuilder(ItemStack result, Ingredient backing, RecipeCategory category)
	{
		this.category = category;
		this.material = backing;
		this.result = result;
	}
	
	public RecipeHandleJsonBuilder criterion(String name, CriterionConditions criterion)
	{
		this.criteria.criterion(name, criterion);
		return this;
	}
	
	public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId)
	{
		if(this.criteria.getCriteria().isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		this.criteria.parent(CraftingRecipeJsonBuilder.ROOT).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
		exporter.accept((RecipeJsonProvider)((Object)new RecipeHandleJsonProvider(recipeId, this.result, "handles", this.category, this.material, this.criteria, recipeId.withPrefixedPath("recipes/"+this.category.getName()+"/"), false)));
	}
	
	public static class RecipeHandleJsonProvider implements RecipeJsonProvider
	{
		private final Identifier recipeId;
		private final RecipeCategory craftingCategory;
		private final String group;
		private final ItemStack output;
		
		private final Ingredient material;
		
		private final Advancement.Builder advancementBuilder;
		private final Identifier advancementId;
		private final boolean showNotification;
		
		public RecipeHandleJsonProvider(Identifier recipeId, ItemStack output, String group, RecipeCategory cat, Ingredient backing, Advancement.Builder advancementBuilder, Identifier advancementId, boolean showNotification)
		{
			this.recipeId = recipeId;
			this.output = output;
			this.craftingCategory = cat;
			this.group = group;
			
			this.material = backing;
			
			this.advancementBuilder = advancementBuilder;
			this.advancementId = advancementId;
			this.showNotification = showNotification;
		}
		
		public void serialize(JsonObject json)
		{
			json.addProperty("category", this.craftingCategory.getName());
			if(!this.group.isEmpty())
				json.addProperty("group", this.group);
			
			json.add("material", this.material.toJson());
			
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", Registries.ITEM.getId(this.output.getItem()).toString());
			json.add("result", jsonObject2);
			json.addProperty("show_notification", this.showNotification);
		}
		
		public Identifier getRecipeId() { return this.recipeId; }
		
		public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.HANDLE_SERIALIZER.get(); }
		
		public JsonObject toAdvancementJson() { return this.advancementBuilder.toJson(); }
		
		public Identifier getAdvancementId() { return this.advancementId; }
	}
}
