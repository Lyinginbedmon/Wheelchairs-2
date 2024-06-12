package com.lying.wheelchairs.data;

import java.util.HashMap;
import java.util.Map;

import com.lying.wheelchairs.data.recipe.RecipeCaneJsonBuilder;
import com.lying.wheelchairs.data.recipe.RecipeCaneSword;
import com.lying.wheelchairs.data.recipe.RecipeHandleJsonBuilder;
import com.lying.wheelchairs.data.recipe.RecipeWheelchairJsonBuilder;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
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
	public static final String GROUP_CHAIRS = Reference.ModInfo.MOD_ID+":wheels";
	public static final String GROUP_CRUTCHES = Reference.ModInfo.MOD_ID+":crutches";
	
	private static final Map<Item, ItemConvertible> WHEEL_GUIDE = new HashMap<>();
	
	public WHCRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generate(RecipeExporter exporter)
	{
		ComplexRecipeJsonBuilder.create((category) -> new RecipeCaneSword()).offerTo(exporter, "cane_sword");
		
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_OAK, Ingredient.ofItems(Blocks.OAK_LOG), "oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_SPRUCE, Ingredient.ofItems(Blocks.SPRUCE_LOG), "spruce_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_BIRCH, Ingredient.ofItems(Blocks.BIRCH_LOG), "birch_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_DARK_OAK, Ingredient.ofItems(Blocks.DARK_OAK_LOG), "dark_oak_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_JUNGLE, Ingredient.ofItems(Blocks.JUNGLE_LOG), "jungle_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_ACACIA, Ingredient.ofItems(Blocks.ACACIA_LOG), "acacia_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_CRIMSON, Ingredient.ofItems(Blocks.CRIMSON_STEM), "crimson_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_WARPED, Ingredient.ofItems(Blocks.WARPED_STEM), "warped_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_MANGROVE, Ingredient.ofItems(Blocks.MANGROVE_LOG), "mangrove_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_CHERRY, Ingredient.ofItems(Blocks.CHERRY_LOG), "cherry_wheelchair");
		offerWheelchairRecipe(exporter, WHCItems.WHEELCHAIR_BAMBOO, Ingredient.ofItems(Blocks.BAMBOO_BLOCK), "bamboo_wheelchair");
		WHEEL_GUIDE.entrySet().forEach(entry -> offerWheelRecipe(exporter, entry.getKey(), entry.getValue()));
		
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_OAK, Blocks.OAK_PLANKS, "oak_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_SPRUCE, Blocks.SPRUCE_PLANKS, "spruce_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_BIRCH, Blocks.BIRCH_PLANKS, "birch_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_DARK_OAK, Blocks.DARK_OAK_PLANKS, "dark_oak_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_JUNGLE, Blocks.JUNGLE_PLANKS, "jungle_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_ACACIA, Blocks.ACACIA_PLANKS, "acacia_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_CRIMSON, Blocks.CRIMSON_PLANKS, "crimson_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_WARPED, Blocks.WARPED_PLANKS, "warped_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_MANGROVE, Blocks.MANGROVE_PLANKS, "mangrove_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_CHERRY, Blocks.CHERRY_PLANKS, "cherry_crutch");
		offerCrutchRecipe(exporter, WHCItems.CRUTCH_BAMBOO, Blocks.BAMBOO_PLANKS, "bamboo_crutch");
		
		offerCaneRecipe(exporter, WHCItems.CANE_OAK, Ingredient.ofItems(Blocks.STRIPPED_OAK_LOG), "oak_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_SPRUCE, Ingredient.ofItems(Blocks.STRIPPED_SPRUCE_LOG), "spruce_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_BIRCH, Ingredient.ofItems(Blocks.STRIPPED_BIRCH_LOG), "birch_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_DARK_OAK, Ingredient.ofItems(Blocks.STRIPPED_DARK_OAK_LOG), "dark_oak_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_ACACIA, Ingredient.ofItems(Blocks.STRIPPED_ACACIA_LOG), "acacia_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_JUNGLE, Ingredient.ofItems(Blocks.STRIPPED_JUNGLE_LOG), "jungle_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_CRIMSON, Ingredient.ofItems(Blocks.STRIPPED_CRIMSON_STEM), "crimson_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_WARPED, Ingredient.ofItems(Blocks.STRIPPED_WARPED_STEM), "warped_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_MANGROVE, Ingredient.ofItems(Blocks.STRIPPED_MANGROVE_LOG), "mangrove_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_CHERRY, Ingredient.ofItems(Blocks.STRIPPED_CHERRY_LOG), "cherry_cane");
		offerCaneRecipe(exporter, WHCItems.CANE_BAMBOO, Ingredient.ofItems(Blocks.STRIPPED_BAMBOO_BLOCK), "bamboo_cane");
		
		offerHandleRecipe(exporter, WHCItems.HANDLE_OAK, Ingredient.ofItems(Items.OAK_BUTTON), "oak_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_SPRUCE, Ingredient.ofItems(Items.SPRUCE_BUTTON), "spruce_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_BIRCH, Ingredient.ofItems(Items.BIRCH_BUTTON), "birch_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_DARK_OAK, Ingredient.ofItems(Items.DARK_OAK_BUTTON), "dark_oak_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_ACACIA, Ingredient.ofItems(Items.ACACIA_BUTTON), "acacia_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_JUNGLE, Ingredient.ofItems(Items.JUNGLE_BUTTON), "jungle_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_CRIMSON, Ingredient.ofItems(Items.CRIMSON_BUTTON), "crimson_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_WARPED, Ingredient.ofItems(Items.WARPED_BUTTON), "warped_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_MANGROVE, Ingredient.ofItems(Items.MANGROVE_BUTTON), "mangrove_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_CHERRY, Ingredient.ofItems(Items.CHERRY_BUTTON), "cherry_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_BAMBOO, Ingredient.ofItems(Items.BAMBOO_BUTTON), "bamboo_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_BONE, Ingredient.ofItems(Items.BONE), "bone_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_IRON, Ingredient.ofItems(Items.IRON_INGOT), "iron_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_GOLD, Ingredient.ofItems(Items.GOLD_INGOT), "gold_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_SKULL, Ingredient.ofItems(Items.SKELETON_SKULL), "skull_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_WITHER, Ingredient.ofItems(Items.WITHER_SKELETON_SKULL), "wither_handle");
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, WHCItems.CONTROLLER)
			.pattern("j").pattern("b")
			.input('j', Items.REDSTONE_TORCH)
			.input('b', Blocks.OBSERVER)
			.criterion(FabricRecipeProvider.hasItem(Items.REDSTONE_TORCH), FabricRecipeProvider.conditionsFromItem(Items.REDSTONE_TORCH))
			.criterion(FabricRecipeProvider.hasItem(Blocks.OBSERVER), FabricRecipeProvider.conditionsFromItem(Blocks.OBSERVER)).offerTo(exporter);
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
	
	private static void offerCaneRecipe(RecipeExporter exporter, Item chair, Ingredient backing, String name)
	{
		RecipeCaneJsonBuilder builder = new RecipeCaneJsonBuilder(chair.getDefaultStack(), backing, RecipeCategory.TRANSPORTATION);
		if(backing.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
	}
	
	private static void offerCrutchRecipe(RecipeExporter exporter, Item crutch, ItemConvertible plank, String name)
	{
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, crutch)
			.pattern("sWs").pattern("s s").pattern(" P ")
			.input('s', Items.STICK).input('W', Ingredient.ofItems(Items.WHITE_WOOL)).input('P', Ingredient.ofItems(plank))
			.group(GROUP_CRUTCHES)
			.criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK))
			.criterion(FabricRecipeProvider.hasItem(Items.WHITE_WOOL), FabricRecipeProvider.conditionsFromItem(Items.WHITE_WOOL))
			.criterion(FabricRecipeProvider.hasItem(plank), FabricRecipeProvider.conditionsFromItem(plank)).offerTo(exporter);
	}
	
	private static void offerHandleRecipe(RecipeExporter exporter, Item handle, Ingredient material, String name)
	{
		RecipeHandleJsonBuilder builder = new RecipeHandleJsonBuilder(handle.getDefaultStack(), material, RecipeCategory.TRANSPORTATION);
		if(material.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(material.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
	}
	
	static
	{
		WHEEL_GUIDE.put(WHCItems.WHEEL_ACACIA, Blocks.ACACIA_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_BIRCH, Blocks.BIRCH_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_BAMBOO, Blocks.BAMBOO_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_CHERRY, Blocks.CHERRY_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_CRIMSON, Blocks.CRIMSON_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_DARK_OAK, Blocks.DARK_OAK_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_JUNGLE, Blocks.JUNGLE_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_MANGROVE, Blocks.MANGROVE_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_OAK, Blocks.OAK_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_SPRUCE, Blocks.SPRUCE_SLAB);
		WHEEL_GUIDE.put(WHCItems.WHEEL_WARPED, Blocks.WARPED_SLAB);
	}
}
