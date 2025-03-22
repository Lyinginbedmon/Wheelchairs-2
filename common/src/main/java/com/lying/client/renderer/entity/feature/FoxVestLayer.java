package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.FoxVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.entity.state.FoxEntityRenderState;
import net.minecraft.util.Identifier;

public class FoxVestLayer extends AbstractVestLayer<FoxEntityRenderState, FoxEntityModel>
{
	private static final Identifier TEXTURE = Reference.ModInfo.prefix("textures/entity/vest_fox.png");
	private static final Identifier TEXTURE_OVERLAY = Reference.ModInfo.prefix("textures/entity/vest_fox_overlay.png");
	
	public FoxVestLayer(FeatureRendererContext<FoxEntityRenderState, FoxEntityModel> context)
	{
		super(context, new FoxVestModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(WHCModelParts.FOX_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
