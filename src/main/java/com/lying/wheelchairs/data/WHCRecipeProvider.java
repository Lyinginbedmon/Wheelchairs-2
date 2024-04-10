package com.lying.wheelchairs.data;

import java.util.Map;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

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
		// FIXME Complex recipe to support wheelchair creation
//		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_INTEGER_SERIALIZER).offerTo(exporter, "note_integer");
		
		WHEEL_GUIDE.entrySet().forEach(entry -> makeWheelRecipe(entry.getKey(), entry.getValue()).offerTo(exporter));
	}
	
	private static ShapedRecipeJsonBuilder makeWheelRecipe(Item wheel, ItemConvertible slab)
	{
		return ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, wheel)
			.pattern(" S ").pattern("SsS").pattern(" S ")
			.input('S', slab).input('s', Items.STICK)
			.group(GROUP_WHEELS)
			.criterion(FabricRecipeProvider.hasItem(slab), FabricRecipeProvider.conditionsFromItem(slab))
			.criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK));
	}
}
