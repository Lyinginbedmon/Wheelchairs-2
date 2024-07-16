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
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class RecipeWalkerJsonBuilder
{
	private final RecipeCategory category;
	private final ItemStack result;
	private final Ingredient strut, platform, wheelL, wheelR, handle;
	private final Advancement.Builder criteria = Advancement.Builder.createUntelemetered();
	
	public RecipeWalkerJsonBuilder(ItemStack result, Ingredient strut, Ingredient platform, RecipeCategory category)
	{
		this(result, strut, platform, Ingredient.ofItems(Items.STICK), Ingredient.fromTag(WHCItemTags.WHEEL), Ingredient.fromTag(WHCItemTags.WHEEL), category);
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
	
	public RecipeWalkerJsonBuilder criterion(String name, CriterionConditions criterion)
	{
		this.criteria.criterion(name, criterion);
		return this;
	}
	
	public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId)
	{
		if(this.criteria.getCriteria().isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		this.criteria.parent(CraftingRecipeJsonBuilder.ROOT).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
		exporter.accept(((RecipeJsonProvider)((Object)new RecipeWalkerJsonProvider(recipeId, this.result, "walkers", this.strut, this.platform, this.handle, this.wheelL, this.wheelR, this.category, this.criteria, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"), true))));
	}
	
	public static class RecipeWalkerJsonProvider implements RecipeJsonProvider
	{
		private final Identifier recipeId;
		private final RecipeCategory craftingCategory;
		private final String group;
		private final ItemStack output;
		
		private final Ingredient strut, platform, wheelL, wheelR, handle;
		
		private final Advancement.Builder advancementBuilder;
		private final Identifier advancementId;
		private final boolean showNotification;
		
		public RecipeWalkerJsonProvider(
				Identifier recipeId, ItemStack output, String group, 
				Ingredient strut, Ingredient platform, Ingredient handle, Ingredient wheelL, Ingredient wheelR, 
				RecipeCategory cat, Advancement.Builder advancementBuilder, Identifier advancementId, boolean showNotification)
		{
			this.recipeId = recipeId;
			this.output = output;
			this.craftingCategory = cat;
			this.group = group;
			
			this.strut = strut;
			this.platform = platform;
			this.handle = handle;
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
			
			json.add("strut", this.strut.toJson());
			json.add("platform", this.platform.toJson());
			json.add("left_wheel", this.wheelL.toJson());
			json.add("right_wheel", this.wheelR.toJson());
			json.add("handle", this.handle.toJson());
			
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", Registries.ITEM.getId(this.output.getItem()).toString());
			json.add("result", jsonObject2);
			json.addProperty("show_notification", this.showNotification);
		}
		
		public Identifier getRecipeId() { return this.recipeId; }
		
		public RecipeSerializer<?> getSerializer() { return WHCSpecialRecipes.WALKER_SERIALIZER.get(); }
		
		public JsonObject toAdvancementJson() { return this.advancementBuilder.toJson(); }
		
		public Identifier getAdvancementId() { return this.advancementId; }
	}
}
