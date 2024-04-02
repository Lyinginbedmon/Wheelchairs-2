package com.lying.wheelchairs.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.wheelchairs.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class WHCBlocks
{
	private static final Map<Identifier, Block> BLOCKS = new HashMap<>();
	
//	public static final Block PRESCIENCE = register("bottle_prescience", new BlockPrescience(FabricBlockSettings.create().luminance((state) -> 8).strength(0.3f).sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY).allowsSpawning(WHCBlocks::never).solidBlock(WHCBlocks::never).suffocates(WHCBlocks::never).blockVision(WHCBlocks::never)));
	
	private static Block register(String nameIn, Block blockIn)
	{
		BLOCKS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), blockIn);
		return blockIn;
	}
	
	public static void init()
	{
		for(Entry<Identifier, Block> entry : BLOCKS.entrySet())
			Registry.register(Registries.BLOCK, entry.getKey(), entry.getValue());
		
//		DispenserBlock.registerBehavior(TFItems.PRESCIENCE_ITEM, new BlockPlacementDispenserBehavior());
	}
	
	private static boolean never(BlockState state, BlockView world, BlockPos pos) { return false; }
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return false; }
}
