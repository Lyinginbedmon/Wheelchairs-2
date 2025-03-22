package com.lying.client.renderer.entity.feature;

import com.lying.client.init.WHCModelParts;
import com.lying.client.renderer.entity.model.WolfVestModel;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.util.Identifier;

public class WolfVestLayer extends AbstractVestLayer<WolfEntityRenderState, WolfEntityModel>
{
	private static final Identifier TEXTURE = Reference.ModInfo.prefix("textures/entity/vest_wolf.png");
	private static final Identifier TEXTURE_OVERLAY = Reference.ModInfo.prefix("textures/entity/vest_wolf_overlay.png");
	
	public WolfVestLayer(FeatureRendererContext<WolfEntityRenderState, WolfEntityModel> context)
	{
		super(context, new WolfVestModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(WHCModelParts.WOLF_VEST)), TEXTURE, TEXTURE_OVERLAY);
	}
}
