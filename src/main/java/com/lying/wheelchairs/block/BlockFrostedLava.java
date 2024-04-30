package com.lying.wheelchairs.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockFrostedLava extends Block
{
	public static final IntProperty AGE = Properties.AGE_3;
	
	public BlockFrostedLava(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState(getDefaultState().with(AGE, 0));
	}
	
	public static BlockState getMeltedState() { return Blocks.LAVA.getDefaultState(); }
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		this.scheduledTick(state, world, pos, random);
	}
	
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
        if((random.nextInt(3) == 0 || this.canMelt(world, pos, 4)) && world.getLightLevel(pos) > 11 - state.get(AGE) - state.getOpacity(world, pos) && this.increaseAge(state, world, pos))
        {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for(Direction direction : Direction.values())
            {
                mutable.set((Vec3i)pos, direction);
                BlockState blockState = world.getBlockState(mutable);
                if(!blockState.isOf(this) || this.increaseAge(blockState, world, mutable))
                	continue;
                world.scheduleBlockTick(mutable, this, MathHelper.nextInt(random, 20, 40));
            }
            return;
        }
        world.scheduleBlockTick(pos, this, MathHelper.nextInt(random, 20, 40));
	}
	
	protected boolean canMelt(BlockView world, BlockPos pos, int maxNeighbours)
	{
		int neighbours=0;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for(Direction direction : Direction.values())
		{
			mutable.set(pos, direction);
			if(!world.getBlockState(mutable).isOf(this) || ++neighbours < maxNeighbours)
				continue;
			return false;
		}
		return true;
	}
	
	protected boolean increaseAge(BlockState state, World world, BlockPos pos)
	{
		int i = state.get(AGE);
		if(i<3)
		{
			world.setBlockState(pos, state.with(AGE, ++i), Block.NOTIFY_LISTENERS);
			return false;
		}
		this.melt(state, world, pos);
		return true;
	}
	
	protected void melt(BlockState state, World world, BlockPos pos)
	{
		world.setBlockState(pos, getMeltedState());
		world.updateNeighbor(pos, getMeltedState().getBlock(), pos);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(AGE);
	}
}
