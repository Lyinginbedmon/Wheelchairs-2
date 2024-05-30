package com.lying.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.block.BlockFrostedLava;
import com.lying.reference.Reference;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

// TODO Ensure blocks used solely for model rendering are inaccessible even with commands
public class WHCBlocks
{
	private static final Map<Identifier, Block> BLOCKS = new HashMap<>();
	
	public static final Block FROSTED_LAVA = register("frosted_lava", new BlockFrostedLava(AbstractBlock.Settings.create().nonOpaque().ticksRandomly().luminance(state -> 3 + state.get(BlockFrostedLava.AGE) * 2).strength(1.5f).allowsSpawning((state, world, pos, entityType) -> entityType.isFireImmune())));
	
	private static Block register(String nameIn, Block blockIn)
	{
		BLOCKS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), blockIn);
		return blockIn;
	}
	
	public static void registerFakeBlock(String nameIn)
	{
		register(nameIn, new FakeBlock(AbstractBlock.Settings.create()));
	}
	
	public static void init()
	{
		for(Entry<Identifier, Block> entry : BLOCKS.entrySet())
			Registry.register(Registries.BLOCK, entry.getKey(), entry.getValue());
	}
	
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return false; }
	
	private static final class FakeBlock extends Block
	{
		public FakeBlock(Settings settings)
		{
			super(settings.allowsSpawning(WHCBlocks::never).dropsNothing());
		}
	}
}
