package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.WolfVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

public class WolfVestLayer extends AbstractVestLayer<WolfEntity, WolfEntityModel<WolfEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_wolf.png");
	private static final Identifier TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/vest_wolf_overlay.png");
	
	public WolfVestLayer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context)
	{
		super(context, new WolfVestModel<WolfEntity>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(WHCModelParts.WOLF_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
