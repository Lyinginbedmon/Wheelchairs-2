package com.lying.wheelchairs.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.data.recipe.RecipeCaneJsonBuilder;
import com.lying.wheelchairs.data.recipe.RecipeCaneSword;
import com.lying.wheelchairs.data.recipe.RecipeHandleJsonBuilder;
import com.lying.wheelchairs.data.recipe.RecipeWalkerJsonBuilder;
import com.lying.wheelchairs.data.recipe.RecipeWheelchairJsonBuilder;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

// TODO Implement REI support for special recipe display
public class WHCRecipeProvider extends FabricRecipeProvider
{
	public static final String GROUP_WHEELS = Reference.ModInfo.MOD_ID+":wheels";
	public static final String GROUP_CHAIRS = Reference.ModInfo.MOD_ID+":wheelchairs";
	public static final String GROUP_CANES = Reference.ModInfo.MOD_ID+":canes";
	public static final String GROUP_CRUTCHES = Reference.ModInfo.MOD_ID+":crutches";
	
	private static final Map<Wood, WoodSet> WOOD_GUIDE = new HashMap<>();
	
	public WHCRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generate(RecipeExporter exporter)
	{
		ComplexRecipeJsonBuilder.create((category) -> new RecipeCaneSword()).offerTo(exporter, "cane_sword");
		
		WOOD_GUIDE.entrySet().forEach(entry -> 
		{
			offerWoodWheelRecipe(exporter, entry.getValue().wheel, entry.getKey());
			offerWheelchairRecipe(exporter, entry.getValue().wheelchair, entry.getKey());
			offerWalkerRecipe(exporter, entry.getValue().walker, entry.getKey());
			offerCrutchRecipe(exporter, entry.getValue().crutch, entry.getKey());
			offerCaneRecipe(exporter, entry.getValue().cane, entry.getKey());
			offerHandleRecipe(exporter, entry.getValue().handle, entry.getKey());
		});
		offerMetalWheelRecipe(exporter, WHCItems.WHEEL_COPPER, Metal.COPPER);
		offerMetalWheelRecipe(exporter, WHCItems.WHEEL_IRON, Metal.IRON);
		offerMetalWheelRecipe(exporter, WHCItems.WHEEL_GOLD, Metal.GOLD);
		offerMetalWheelRecipe(exporter, WHCItems.WHEEL_NETHERITE, Metal.NETHERITE);
		
		offerHandleRecipe(exporter, WHCItems.HANDLE_BONE, Ingredient.ofItems(Items.BONE), "bone_handle");
		offerHandleRecipe(exporter, WHCItems.HANDLE_COPPER, Ingredient.ofItems(Items.COPPER_INGOT), "copper_handle");
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
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, WHCItems.STOOL)
			.pattern(" s ").pattern(" i ").pattern("wbw")
			.input('s', ItemTags.WOOL)
			.input('i', Items.IRON_INGOT)
			.input('b', Items.IRON_BARS)
			.input('w', WHCItemTags.WHEEL)
			.criterion(FabricRecipeProvider.hasItem(WHCItems.WHEEL_OAK), FabricRecipeProvider.conditionsFromTag(WHCItemTags.WHEEL))
			.criterion(FabricRecipeProvider.hasItem(Items.IRON_BARS), FabricRecipeProvider.conditionsFromItem(Items.IRON_BARS)).offerTo(exporter);
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, WHCItems.VEST)
			.pattern(" l ").pattern("lil").pattern(" s ")
			.input('l', Items.LEATHER)
			.input('s', Items.STRING)
			.input('i', Items.GOLD_INGOT)
			.criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
			.criterion(FabricRecipeProvider.hasItem(Items.STRING), FabricRecipeProvider.conditionsFromItem(Items.STRING)).offerTo(exporter);
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, WHCItems.TABLET)
			.pattern("bb").pattern("ns")
			.input('n', Blocks.NOTE_BLOCK)
			.input('b', Blocks.STONE_BUTTON)
			.input('s', Blocks.SMOOTH_STONE_SLAB)
			.criterion(FabricRecipeProvider.hasItem(Blocks.NOTE_BLOCK), FabricRecipeProvider.conditionsFromItem(Blocks.NOTE_BLOCK))
			.criterion(FabricRecipeProvider.hasItem(Blocks.STONE_BUTTON), FabricRecipeProvider.conditionsFromItem(Blocks.STONE_BUTTON))
			.criterion(FabricRecipeProvider.hasItem(Blocks.SMOOTH_STONE_SLAB), FabricRecipeProvider.conditionsFromItem(Blocks.SMOOTH_STONE_SLAB)).offerTo(exporter);
	}
	
	private static void offerWoodWheelRecipe(RecipeExporter exporter, Item wheel, Wood wood)
	{
		offerWheelRecipe(exporter, wheel, wood.slab);
	}
	
	private static void offerMetalWheelRecipe(RecipeExporter exporter, Item wheel, Metal metal)
	{
		offerWheelRecipe(exporter, wheel, metal.ingot);
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
	
	private static void offerWheelchairRecipe(RecipeExporter exporter, Item chair, Wood wood)
	{
		String name = wood.name().toLowerCase()+"_wheelchair";
		Ingredient backing = Ingredient.ofItems(wood.log);
		
		// Primary customisable recipe
		RecipeWheelchairJsonBuilder builder = new RecipeWheelchairJsonBuilder(chair.getDefaultStack(), backing, RecipeCategory.TRANSPORTATION);
		if(backing.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
		
		// Secondary basic recipe
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, chair).group(GROUP_CHAIRS)
			.pattern(" b ")
			.pattern("wWw")
			.input('b', backing)
			.input('w', WHCItemTags.WHEEL)
			.input('W', ItemTags.WOOL)
			.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem())).offerTo(exporter, name+"_basic");
	}
	
	private static void offerCaneRecipe(RecipeExporter exporter, Item cane, Wood wood)
	{
		String name = wood.name().toLowerCase()+"_cane";
		Ingredient backing = Ingredient.ofItems(wood.strippedLog);
		
		// Primary customisable recipe
		RecipeCaneJsonBuilder builder = new RecipeCaneJsonBuilder(cane.getDefaultStack(), backing, RecipeCategory.TRANSPORTATION);
		if(backing.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
		
		// Secondary basic recipe
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, cane).group(GROUP_CANES)
			.pattern(" h ")
			.pattern(" b ")
			.pattern(" s ")
			.input('h', wood.button)
			.input('b', backing)
			.input('s', Items.STICK)
			.criterion("has_backing", RecipeProvider.conditionsFromItem(backing.getMatchingStacks()[0].getItem())).offerTo(exporter, name+"_basic");
	}
	
	private static void offerHandleRecipe(RecipeExporter exporter, Item handle, Wood wood)
	{
		Ingredient material = Ingredient.ofItems(wood.button);
		RecipeHandleJsonBuilder builder = new RecipeHandleJsonBuilder(handle.getDefaultStack(), material, RecipeCategory.TRANSPORTATION);
		if(material.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(material.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, wood.name().toLowerCase()+"_handle"));
	}
	
	private static void offerHandleRecipe(RecipeExporter exporter, Item handle, Ingredient material, String name)
	{
		RecipeHandleJsonBuilder builder = new RecipeHandleJsonBuilder(handle.getDefaultStack(), material, RecipeCategory.TRANSPORTATION);
		if(material.getMatchingStacks().length > 0)
			builder.criterion("has_backing", RecipeProvider.conditionsFromItem(material.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
	}
	
	private static void offerCrutchRecipe(RecipeExporter exporter, Item crutch, Wood wood)
	{
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, crutch)
			.pattern("sWs").pattern("s s").pattern(" P ")
			.input('s', Items.STICK).input('W', Ingredient.ofItems(Items.WHITE_WOOL)).input('P', Ingredient.ofItems(wood.planks))
			.group(GROUP_CRUTCHES)
			.criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK))
			.criterion(FabricRecipeProvider.hasItem(Items.WHITE_WOOL), FabricRecipeProvider.conditionsFromItem(Items.WHITE_WOOL))
			.criterion(FabricRecipeProvider.hasItem(wood.planks), FabricRecipeProvider.conditionsFromItem(wood.planks)).offerTo(exporter);
	}
	
	private static void offerWalkerRecipe(RecipeExporter exporter, Item walker, Wood wood)
	{
		String name = wood.name().toLowerCase()+"_walker";
		Ingredient strut = Ingredient.ofItems(wood.log);
		Ingredient platform = Ingredient.ofItems(wood.planks);
		
		// Primary customisable recipe
		RecipeWalkerJsonBuilder builder = new RecipeWalkerJsonBuilder(walker.getDefaultStack(), strut, platform, RecipeCategory.TRANSPORTATION);
		if(strut.getMatchingStacks().length > 0)
			builder.criterion("has_strut", RecipeProvider.conditionsFromItem(strut.getMatchingStacks()[0].getItem()));
		if(platform.getMatchingStacks().length > 0)
			builder.criterion("has_platform", RecipeProvider.conditionsFromItem(platform.getMatchingStacks()[0].getItem()));
		builder.offerTo(exporter, new Identifier(Reference.ModInfo.MOD_ID, name));
		
		// Secondary basic recipe
		ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, walker)
			.pattern("s s")
			.pattern("lpl")
			.pattern("w w")
			.input('s', Items.STICK)
			.input('l', strut)
			.input('p', platform)
			.input('w', WHCItemTags.WHEEL)
			.criterion("has_strut", RecipeProvider.conditionsFromItem(strut.getMatchingStacks()[0].getItem()))
			.criterion("has_platform", RecipeProvider.conditionsFromItem(platform.getMatchingStacks()[0].getItem())).offerTo(exporter, name+"_basic");
	}
	
	private static void putWoodSet(Wood wood, Item wheel, Item chair, Item walker, Item crutch, Item cane, Item handle)
	{
		WOOD_GUIDE.put(wood, new WoodSet(wheel, chair, walker, crutch, cane, handle));
	}
	
	static
	{
		putWoodSet(Wood.OAK, WHCItems.WHEEL_OAK, WHCItems.WHEELCHAIR_OAK, WHCItems.WALKER_OAK, WHCItems.CRUTCH_OAK, WHCItems.CANE_OAK, WHCItems.HANDLE_OAK);
		putWoodSet(Wood.SPRUCE, WHCItems.WHEEL_SPRUCE, WHCItems.WHEELCHAIR_SPRUCE, WHCItems.WALKER_SPRUCE, WHCItems.CRUTCH_SPRUCE, WHCItems.CANE_SPRUCE, WHCItems.HANDLE_SPRUCE);
		putWoodSet(Wood.BIRCH, WHCItems.WHEEL_BIRCH, WHCItems.WHEELCHAIR_BIRCH, WHCItems.WALKER_BIRCH, WHCItems.CRUTCH_BIRCH, WHCItems.CANE_BIRCH, WHCItems.HANDLE_BIRCH);
		putWoodSet(Wood.DARK_OAK, WHCItems.WHEEL_DARK_OAK, WHCItems.WHEELCHAIR_DARK_OAK, WHCItems.WALKER_DARK_OAK, WHCItems.CRUTCH_DARK_OAK, WHCItems.CANE_DARK_OAK, WHCItems.HANDLE_DARK_OAK);
		putWoodSet(Wood.JUNGLE, WHCItems.WHEEL_JUNGLE, WHCItems.WHEELCHAIR_JUNGLE, WHCItems.WALKER_JUNGLE, WHCItems.CRUTCH_JUNGLE, WHCItems.CANE_JUNGLE, WHCItems.HANDLE_JUNGLE);
		putWoodSet(Wood.ACACIA, WHCItems.WHEEL_ACACIA, WHCItems.WHEELCHAIR_ACACIA, WHCItems.WALKER_ACACIA, WHCItems.CRUTCH_ACACIA, WHCItems.CANE_ACACIA, WHCItems.HANDLE_ACACIA);
		putWoodSet(Wood.CRIMSON, WHCItems.WHEEL_CRIMSON, WHCItems.WHEELCHAIR_CRIMSON, WHCItems.WALKER_CRIMSON, WHCItems.CRUTCH_CRIMSON, WHCItems.CANE_CRIMSON, WHCItems.HANDLE_CRIMSON);
		putWoodSet(Wood.WARPED, WHCItems.WHEEL_WARPED, WHCItems.WHEELCHAIR_WARPED, WHCItems.WALKER_WARPED, WHCItems.CRUTCH_WARPED, WHCItems.CANE_WARPED, WHCItems.HANDLE_WARPED);
		putWoodSet(Wood.MANGROVE, WHCItems.WHEEL_MANGROVE, WHCItems.WHEELCHAIR_MANGROVE, WHCItems.WALKER_MANGROVE, WHCItems.CRUTCH_MANGROVE, WHCItems.CANE_MANGROVE, WHCItems.HANDLE_MANGROVE);
		putWoodSet(Wood.CHERRY, WHCItems.WHEEL_CHERRY, WHCItems.WHEELCHAIR_CHERRY, WHCItems.WALKER_CHERRY, WHCItems.CRUTCH_CHERRY, WHCItems.CANE_CHERRY, WHCItems.HANDLE_CHERRY);
		putWoodSet(Wood.BAMBOO, WHCItems.WHEEL_BAMBOO, WHCItems.WHEELCHAIR_BAMBOO, WHCItems.WALKER_BAMBOO, WHCItems.CRUTCH_BAMBOO, WHCItems.CANE_BAMBOO, WHCItems.HANDLE_BAMBOO);
	}
	
	public static class WoodSet
	{
		private final Item wheelchair, walker, crutch, cane, handle, wheel;
		
		public WoodSet(Item wheel, Item chair, Item walker, Item crutch, Item cane, Item handle)
		{
			this.wheel = wheel;
			this.wheelchair = chair;
			this.walker = walker;
			this.crutch = crutch;
			this.cane = cane;
			this.handle = handle;
		}
	}
	
	/** Convenient data object for holding different related wooden blocks */
	public static enum Wood
	{
		OAK(Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_BUTTON),
		SPRUCE(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_BUTTON),
		BIRCH(Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_BUTTON),
		DARK_OAK(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_BUTTON),
		JUNGLE(Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_BUTTON),
		ACACIA(Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_BUTTON),
		CRIMSON(Blocks.CRIMSON_STEM, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_BUTTON),
		WARPED(Blocks.WARPED_STEM, Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_BUTTON),
		MANGROVE(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_BUTTON),
		CHERRY(Blocks.CHERRY_LOG, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_BUTTON),
		BAMBOO(Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_BUTTON);
		
		public final Block log, planks, slab, strippedLog, button;
		
		private Wood(Block logIn, Block planksIn, Block slabIn, Block strippedLogIn, Block buttonIn)
		{
			this.log = logIn;
			this.planks = planksIn;
			this.slab = slabIn;
			this.strippedLog = strippedLogIn;
			this.button = buttonIn;
		}
	}
	
	public static enum Metal
	{
		COPPER(Items.COPPER_INGOT, Blocks.COPPER_BLOCK),
		IRON(Items.IRON_INGOT, Blocks.IRON_BLOCK, Items.IRON_NUGGET),
		GOLD(Items.GOLD_INGOT, Blocks.GOLD_BLOCK, Items.GOLD_NUGGET),
		NETHERITE(Items.NETHERITE_INGOT, Blocks.NETHERITE_BLOCK);
		
		public final Item ingot;
		public final Block block;
		public final Optional<Item> nugget;
		
		private Metal(Item ingotIn, Block blockIn)
		{
			this(ingotIn, blockIn, null);
		}
		
		private Metal(Item ingotIn, Block blockIn, @Nullable Item nuggetIn)
		{
			this.ingot = ingotIn;
			this.block = blockIn;
			this.nugget = nuggetIn == null ? Optional.empty() : Optional.of(nuggetIn);
		}
	}
}
