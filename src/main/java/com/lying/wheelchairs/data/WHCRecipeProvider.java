package com.lying.wheelchairs.data;

import java.util.Map;

import com.lying.wheelchairs.data.recipe.RecipeWheelchairJsonBuilder;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class WHCRecipeProvider extends FabricRecipeProvider
{
	public static final String GROUP_WHEELS = Reference.ModInfo.MOD_ID+":wheels";
	
	private static final Map<Item, ItemConvertible> WHEEL_GUIDE = Map.of(
			WHCItems.WHEEL_ACACIA, Blocks.ACACIA_SLAB,
			WHCItems.WHEEL_BIRCH, Blocks.BIRCH_SLAB,
			WHCItems.WHEEL_CRIMSON, Blocks.CRIMSON_SLAB,
			WHCItems.WHEEL_DARK_OAK, Blocks.DARK_OAK_SLAB,
			WHCItems.WHEEL_JUNGLE, Blocks.JUNGLE_SLAB,
			WHCItems.WHEEL_MANGROVE, Blocks.MANGROVE_SLAB,
			WHCItems.WHEEL_OAK, Blocks.OAK_SLAB,
			WHCItems.WHEEL_SPRUCE, Blocks.SPRUCE_SLAB,
			WHCItems.WHEEL_WARPED, Blocks.WARPED_SLAB
			);
	
	public WHCRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generate(RecipeExporter exporter)
	{
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_OAK, Ingredient.ofItems(Blocks.OAK_LOG), "oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_SPRUCE, Ingredient.ofItems(Blocks.SPRUCE_LOG), "spruce_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_BIRCH, Ingredient.ofItems(Blocks.BIRCH_LOG), "birch_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_DARK_OAK, Ingredient.ofItems(Blocks.DARK_OAK_LOG), "dark_oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_JUNGLE, Ingredient.ofItems(Blocks.JUNGLE_LOG), "jungle_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_ACACIA, Ingredient.ofItems(Blocks.ACACIA_LOG), "acacia_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_CRIMSON, Ingredient.ofItems(Blocks.CRIMSON_STEM), "crimson_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_WARPED, Ingredient.ofItems(Blocks.WARPED_STEM), "warped_wheelchair");
		WHEEL_GUIDE.entrySet().forEach(entry -> offerWheelRecipe(exporter, entry.getKey(), entry.getValue()));
	}
	
	private static void offerWheelRecipe(RecipeExporter exporter, Item wheel, ItemConvertible slab)
	{
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, wheel)
			.pattern(" S ").pattern("SsS").pattern(" S ")
			.input('S', slab).input('s', Items.STICK)
			.group(GROUP_WHEELS)
			.criterion(FabricRecipeProvider.hasItem(slab), FabricRecipeProvider.conditionsFromItem(slab))
			.criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK)).offerTo(exporter);
	}
	
	private static void offerWheelchairRecipe(RecipeExporter exporter, Item chair, Ingredient backing, String name)
	{
		RecipeWheelchairJsonBuilder builder = new RecipeWheelchairJsonBuilder(chair.getDefaultStack(), backing, RecipeCategory.TRANSPORTATION);
		if(backing.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
	}
}
