package com.lying.wheelchairs.renderer.block;

import com.lying.wheelchairs.init.WHCItems;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

public class WHCSpecialModels implements ModelLoadingPlugin
{
	private static final StateManager<Block, BlockState> stateManager = 
			new StateManager.Builder<Block, BlockState>(Blocks.AIR)
			.add(new Property[] {BooleanProperty.of((String)"overlay")})
			.build(Block::getDefaultState, BlockState::new);
	
	public void onInitializeModelLoader(Context pluginContext)
	{
		// FIXME Ensure models are actually loaded according to blockstate json
		WHCItems.WHEELCHAIRS.forEach(chair -> 
		{
			Identifier id = Registries.ITEM.getId(chair);
			stateManager.getStates().forEach(state -> pluginContext.addModels(BlockModels.getModelId(id, state)));
		});
	}
}
