package com.lying.wheelchairs.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.init.WHCItemsClient;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelLoader.SourceTrackedData;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin
{
	@Shadow
	private Map<Identifier, UnbakedModel> modelsToBake;
	
	@Shadow
	public void addModel(ModelIdentifier model) { }
	
	@Shadow
	public UnbakedModel getOrLoadModel(Identifier id) { return null; }
	
	@Inject(method = "<init>", at = @At("TAIL"))
	public void whc$init(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<SourceTrackedData>> blockStates, final CallbackInfo ci)
	{
		WHCItemsClient.getExtraModelsToRegister().forEach(model -> addModel(model));
		modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
	}
}
