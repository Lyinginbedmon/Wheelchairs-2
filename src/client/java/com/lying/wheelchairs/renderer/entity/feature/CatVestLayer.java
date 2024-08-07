package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.CatVestModel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.Identifier;

public class CatVestLayer extends AbstractVestLayer<CatEntity, CatEntityModel<CatEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_cat.png");
	private static final Identifier TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_cat_overlay.png");
	
	public CatVestLayer(FeatureRendererContext<CatEntity, CatEntityModel<CatEntity>> context)
	{
		super(context, new CatVestModel<CatEntity>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(WHCModelParts.CAT_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
