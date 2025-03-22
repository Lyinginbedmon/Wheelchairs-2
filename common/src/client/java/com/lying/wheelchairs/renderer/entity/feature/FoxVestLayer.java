package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.FoxVestModel;

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
