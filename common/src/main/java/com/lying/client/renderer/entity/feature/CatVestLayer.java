package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.CatVestModel;
import com.lying.reference.Reference;

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
