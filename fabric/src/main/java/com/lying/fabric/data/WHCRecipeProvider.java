package com.lying.fabric.data;

import java.util.Map;
import java.util.function.Consumer;

import com.lying.data.recipe.RecipeWheelchairJsonBuilder;
import com.lying.init.WHCItems;
import com.lying.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
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
			WHCItems.WHEEL_ACACIA.get(), Blocks.ACACIA_SLAB,
			WHCItems.WHEEL_BIRCH.get(), Blocks.BIRCH_SLAB,
			WHCItems.WHEEL_CRIMSON.get(), Blocks.CRIMSON_SLAB,
			WHCItems.WHEEL_DARK_OAK.get(), Blocks.DARK_OAK_SLAB,
			WHCItems.WHEEL_JUNGLE.get(), Blocks.JUNGLE_SLAB,
			WHCItems.WHEEL_MANGROVE.get(), Blocks.MANGROVE_SLAB,
			WHCItems.WHEEL_OAK.get(), Blocks.OAK_SLAB,
			WHCItems.WHEEL_SPRUCE.get(), Blocks.SPRUCE_SLAB,
			WHCItems.WHEEL_WARPED.get(), Blocks.WARPED_SLAB
			);
	
	public WHCRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generate(Consumer<RecipeJsonProvider> exporter)
	{
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_OAK.get(), Ingredient.ofItems(Blocks.OAK_LOG), "oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_SPRUCE.get(), Ingredient.ofItems(Blocks.SPRUCE_LOG), "spruce_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_BIRCH.get(), Ingredient.ofItems(Blocks.BIRCH_LOG), "birch_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_DARK_OAK.get(), Ingredient.ofItems(Blocks.DARK_OAK_LOG), "dark_oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_JUNGLE.get(), Ingredient.ofItems(Blocks.JUNGLE_LOG), "jungle_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_ACACIA.get(), Ingredient.ofItems(Blocks.ACACIA_LOG), "acacia_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_CRIMSON.get(), Ingredient.ofItems(Blocks.CRIMSON_STEM), "crimson_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_WARPED.get(), Ingredient.ofItems(Blocks.WARPED_STEM), "warped_wheelchair");
		WHEEL_GUIDE.entrySet().forEach(entry -> offerWheelRecipe(exporter, entry.getKey(), entry.getValue()));
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, WHCItems.CONTROLLER.get())
			.pattern("j").pattern("b")
			.input('j', Items.REDSTONE_TORCH)
			.input('b', Blocks.OBSERVER)
			.criterion(RecipeProvider.hasItem(Items.REDSTONE_TORCH), RecipeProvider.conditionsFromItem(Items.REDSTONE_TORCH))
			.criterion(RecipeProvider.hasItem(Blocks.OBSERVER), RecipeProvider.conditionsFromItem(Blocks.OBSERVER)).offerTo(exporter);
	}
	
	private static void offerWheelRecipe(Consumer<RecipeJsonProvider> exporter, Item wheel, ItemConvertible slab)
	{
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, wheel)
			.pattern(" S ").pattern("SsS").pattern(" S ")
			.input('S', slab).input('s', Items.STICK)
			.group(GROUP_WHEELS)
			.criterion(RecipeProvider.hasItem(slab), RecipeProvider.conditionsFromItem(slab))
			.criterion(RecipeProvider.hasItem(Items.STICK), RecipeProvider.conditionsFromItem(Items.STICK)).offerTo(exporter);
	}
	
	private static void offerWheelchairRecipe(Consumer<RecipeJsonProvider> exporter, Item chair, Ingredient backing, String name)
	{
		RecipeWheelchairJsonBuilder builder = new RecipeWheelchairJsonBuilder(chair.getDefaultStack(), backing, RecipeCategory.TRANSPORTATION);
		if(backing.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
	}
}
