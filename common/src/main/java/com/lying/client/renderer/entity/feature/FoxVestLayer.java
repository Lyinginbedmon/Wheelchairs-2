package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.FoxVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.Identifier;

public class FoxVestLayer extends AbstractVestLayer<FoxEntity, FoxEntityModel<FoxEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_fox.png");
	private static final Identifier TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_fox_overlay.png");
	
	public FoxVestLayer(FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>> context)
	{
		super(context, new FoxVestModel<FoxEntity>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(WHCModelParts.FOX_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
