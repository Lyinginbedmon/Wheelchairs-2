package com.lying.data.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.lying.data.WHCItemTags;
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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class RecipeWheelchairJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient backing, cushion, wheelL, wheelR;
	private final Advancement.Builder criteria = Advancement.Builder.createUntelemetered();
	
	public RecipeWheelchairJsonBuilder(ItemStack result, Ingredient backing, RecipeCategory category)
	{
		this(result, backing, Ingredient.fromTag(ItemTags.WOOL), Ingredient.fromTag(WHCItemTags.WHEEL), Ingredient.fromTag(WHCItemTags.WHEEL), category);
	}
	
	public RecipeWheelchairJsonBuilder(ItemStack result, Ingredient backing, Ingredient cushion, Ingredient wheelL, Ingredient wheelR, RecipeCategory category)
	{
		this.category = category;
		this.backing = backing;
		this.cushion = cushion;
		this.wheelL = wheelL;
		this.wheelR = wheelR;
		this.result = result;
	}
	
	public RecipeWheelchairJsonBuilder criterion(String name, CriterionConditions criterion)
	{
		this.criteria.criterion(name, criterion);
		return this;
	}
	
	public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId)
	{
		if(this.criteria.getCriteria().isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		this.criteria.parent(CraftingRecipeJsonBuilder.ROOT).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
        exporter.accept((RecipeJsonProvider)((Object)new RecipeWheelchairJsonProvider(recipeId, this.result, "wheelchairs", this.category, this.backing, this.cushion, this.wheelL, this.wheelR, this.criteria, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"), true)));
	}
	
	public static class RecipeWheelchairJsonProvider implements RecipeJsonProvider
	{
		private final Identifier recipeId;
		private final RecipeCategory craftingCategory;
		private final String group;
		private final ItemStack output;
		
		private final Ingredient backing, cushion, wheelL, wheelR;
		
		private final Advancement.Builder advancementBuilder;
		private final Identifier advancementId;
		private final boolean showNotification;
		
		public RecipeWheelchairJsonProvider(Identifier recipeId, ItemStack output, String group, RecipeCategory cat, Ingredient backing, Ingredient cushion, Ingredient wheelL, Ingredient wheelR, Advancement.Builder advancementBuilder, Identifier advancementId, boolean showNotification)
		{
			this.recipeId = recipeId;
			this.output = output;
			this.craftingCategory = cat;
			this.group = group;
			
			this.backing = backing;
			this.cushion = cushion;
			this.wheelL = wheelL;
			this.wheelR = wheelR;
			
			this.advancementBuilder = advancementBuilder;
			this.advancementId = advancementId;
			this.showNotification = showNotification;
		}
		
		public void serialize(JsonObject json)
		{
			json.addProperty("category", this.craftingCategory.getName());
			if(!this.group.isEmpty())
				json.addProperty("group", this.group);
			
			json.add("backing", this.backing.toJson());
			json.add("cushion", this.cushion.toJson());
			json.add("left_wheel", this.wheelL.toJson());
			json.add("right_wheel", this.wheelR.toJson());
			
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", Registries.ITEM.getId(this.output.getItem()).toString());
			json.add("result", jsonObject2);
			json.addProperty("show_notification", this.showNotification);
		}
		
		public Identifier getRecipeId() { return this.recipeId; }
		
		public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.WHEELCHAIR_SERIALIZER.get(); }
		
		public JsonObject toAdvancementJson() { return this.advancementBuilder.toJson(); }
		
		public Identifier getAdvancementId() { return this.advancementId; }
	}
}
