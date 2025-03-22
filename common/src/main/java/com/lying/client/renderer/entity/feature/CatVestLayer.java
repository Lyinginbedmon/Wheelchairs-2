package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.CatVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.state.CatEntityRenderState;
import net.minecraft.util.Identifier;

public class CatVestLayer extends AbstractVestLayer<CatEntityRenderState, CatEntityModel>
{
	private static final Identifier TEXTURE = Reference.ModInfo.prefix("textures/entity/vest_cat.png");
	private static final Identifier TEXTURE_OVERLAY = Reference.ModInfo.prefix("textures/entity/vest_cat_overlay.png");
	
	public CatVestLayer(FeatureRendererContext<CatEntityRenderState, CatEntityModel> context)
	{
		super(context, new CatVestModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(WHCModelParts.CAT_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
