package com.lying.wheelchairs.init;

import com.lying.wheelchairs.model.WolfVestModel;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.renderer.entity.model.WheelchairElytraModel;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class WHCModelParts
{
	public static final EntityModelLayer UPGRADE_ELYTRA	= ofName("wheelchair_elytra", "main");
	public static final EntityModelLayer WOLF_VEST	= ofName("vest", "wolf");
	
	private static EntityModelLayer ofName(String main, String part)
	{
		return new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, main), part);
	}
	
	public static void init()
	{
		register(WHCModelParts.UPGRADE_ELYTRA, WheelchairElytraModel::createBodyLayer);
		register(WHCModelParts.WOLF_VEST, WolfVestModel::getTexturedModelData);
	}
	
	private static void register(EntityModelLayer layer, TexturedModelDataProvider func)
	{
		EntityModelLayerRegistry.registerModelLayer(layer, func);
	}
}
