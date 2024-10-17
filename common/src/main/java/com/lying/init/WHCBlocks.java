package com.lying.init;

import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.block.BlockFrostedLava;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

// TODO Ensure blocks used solely for model rendering are inaccessible even with commands
public class WHCBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	private static int tally = 0;
	
	public static final RegistrySupplier<Block> FROSTED_LAVA = register("frosted_lava", () -> new BlockFrostedLava(AbstractBlock.Settings.create().nonOpaque().ticksRandomly().luminance(state -> 3 + state.get(BlockFrostedLava.AGE) * 2).strength(1.5f).allowsSpawning((state, world, pos, entityType) -> entityType.isFireImmune())));
	
	private static RegistrySupplier<Block> register(String nameIn, Supplier<Block> blockIn)
	{
		++tally;
		return BLOCKS.register(new Identifier(Reference.ModInfo.MOD_ID, nameIn), blockIn);
	}
	
	public static RegistrySupplier<Block> registerFakeBlock(String nameIn)
	{
		return register(nameIn, () -> new FakeBlock(AbstractBlock.Settings.create()));
	}
	
	public static void init()
	{
		BLOCKS.register();
		Wheelchairs.LOGGER.info(" # Registered "+tally+" blocks");
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
