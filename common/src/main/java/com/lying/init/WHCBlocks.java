package com.lying.init;

import java.util.function.Function;

import com.lying.block.FrostedLavaBlock;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

// TODO Ensure blocks used solely for model rendering are inaccessible even with commands
public class WHCBlocks
{
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	
	public static final RegistrySupplier<Block> FROSTED_LAVA = register("frosted_lava", settings -> new FrostedLavaBlock(settings.nonOpaque().ticksRandomly().luminance(state -> 3 + state.get(FrostedLavaBlock.AGE) * 2).strength(1.5f).allowsSpawning((state, world, pos, entityType) -> entityType.isFireImmune())));
	
	private static RegistrySupplier<Block> register(String nameIn, Function<AbstractBlock.Settings,Block> blockIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.create().registryKey(key);
		return BLOCKS.register(id, () -> blockIn.apply(settings));
	}
	
	public static void registerFakeBlock(String nameIn)
	{
		register(nameIn, settings -> new FakeBlock(settings));
	}
	
	public static void init()
	{
		BLOCKS.register();
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
