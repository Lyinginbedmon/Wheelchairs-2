package com.lying.wheelchairs.init;

import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.CatVestModel;
import com.lying.wheelchairs.renderer.entity.model.FoxVestModel;
import com.lying.wheelchairs.renderer.entity.model.ParrotVestModel;
import com.lying.wheelchairs.renderer.entity.model.WheelchairElytraModel;
import com.lying.wheelchairs.renderer.entity.model.WolfVestModel;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class WHCModelParts
{
	public static final EntityModelLayer UPGRADE_ELYTRA	= ofName("wheelchair_elytra", "main");
	public static final EntityModelLayer WOLF_VEST	= ofName("vest", "wolf");
	public static final EntityModelLayer CAT_VEST	= ofName("vest", "cat");
	public static final EntityModelLayer PARROT_VEST	= ofName("vest", "parrot");
	public static final EntityModelLayer FOX_VEST		= ofName("vest", "fox");
	
	private static EntityModelLayer ofName(String main, String part)
	{
		return new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, main), part);
	}
	
	public static void init()
	{
		register(WHCModelParts.UPGRADE_ELYTRA, WheelchairElytraModel::createBodyLayer);
		register(WHCModelParts.WOLF_VEST, WolfVestModel::getTexturedModelData);
		register(WHCModelParts.CAT_VEST, () -> TexturedModelData.of(CatVestModel.getModelData(Dilation.NONE), 64, 32));
		register(WHCModelParts.PARROT_VEST, ParrotVestModel::getTexturedModelData);
		register(WHCModelParts.FOX_VEST, FoxVestModel::getTexturedModelData);
	}
	
	private static void register(EntityModelLayer layer, TexturedModelDataProvider func)
	{
		EntityModelLayerRegistry.registerModelLayer(layer, func);
	}
}
