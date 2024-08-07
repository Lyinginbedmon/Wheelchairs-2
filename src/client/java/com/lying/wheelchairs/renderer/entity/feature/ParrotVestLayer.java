package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.ParrotVestModel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.Identifier;

public class ParrotVestLayer extends AbstractVestLayer<ParrotEntity, ParrotEntityModel>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_parrot.png");
	private static final Identifier TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_parrot_overlay.png");
	
	public ParrotVestLayer(FeatureRendererContext<ParrotEntity, ParrotEntityModel> context)
	{
		super(context, new ParrotVestModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(WHCModelParts.PARROT_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
