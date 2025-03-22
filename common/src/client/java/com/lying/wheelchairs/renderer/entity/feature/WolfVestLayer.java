package com.lying.wheelchairs.renderer.entity.feature;

import com.lying.wheelchairs.init.WHCModelParts;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.WolfVestModel;

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
