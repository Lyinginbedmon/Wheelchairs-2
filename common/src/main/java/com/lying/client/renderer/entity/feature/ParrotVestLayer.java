package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.ParrotVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.state.ParrotEntityRenderState;
import net.minecraft.util.Identifier;

public class ParrotVestLayer extends AbstractVestLayer<ParrotEntityRenderState, ParrotEntityModel>
{
	private static final Identifier TEXTURE = Reference.ModInfo.prefix("textures/entity/vest_parrot.png");
	private static final Identifier TEXTURE_OVERLAY = Reference.ModInfo.prefix("textures/entity/vest_parrot_overlay.png");
	
	public ParrotVestLayer(FeatureRendererContext<ParrotEntityRenderState, ParrotEntityModel> context)
	{
		super(context, new ParrotVestModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(WHCModelParts.PARROT_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
