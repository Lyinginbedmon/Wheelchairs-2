package com.lying.wheelchairs.init;

import com.lying.wheelchairs.model.entity.ModelWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WHCModelParts
{
	public static final EntityModelLayer WHEELCHAIR	= ofName("wheelchair", "main");
	
	private static EntityModelLayer ofName(String main, String part)
	{
		return new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, main), part);
	}
	
	public static void init()
	{
		register(WHCModelParts.WHEELCHAIR, ModelWheelchair::getMainModel);
	}
	
	private static void register(EntityModelLayer layer, TexturedModelDataProvider func)
	{
		EntityModelLayerRegistry.registerModelLayer(layer, func);
	}
}
