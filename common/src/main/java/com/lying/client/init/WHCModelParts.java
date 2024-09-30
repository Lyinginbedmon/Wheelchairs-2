package com.lying.client.init;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.lying.client.renderer.entity.model.CatVestModel;
import com.lying.client.renderer.entity.model.FoxVestModel;
import com.lying.client.renderer.entity.model.ParrotVestModel;
import com.lying.client.renderer.entity.model.WheelchairElytraModel;
import com.lying.client.renderer.entity.model.WolfVestModel;
import com.lying.reference.Reference;

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
	
	public static void init(BiConsumer<EntityModelLayer, Supplier<TexturedModelData>> consumer)
	{
		consumer.accept(WHCModelParts.UPGRADE_ELYTRA, WheelchairElytraModel::createBodyLayer);
		consumer.accept(WHCModelParts.WOLF_VEST, WolfVestModel::getTexturedModelData);
		consumer.accept(WHCModelParts.CAT_VEST, () -> TexturedModelData.of(CatVestModel.getModelData(Dilation.NONE), 64, 32));
		consumer.accept(WHCModelParts.PARROT_VEST, ParrotVestModel::getTexturedModelData);
		consumer.accept(WHCModelParts.FOX_VEST, FoxVestModel::getTexturedModelData);
	}
}
