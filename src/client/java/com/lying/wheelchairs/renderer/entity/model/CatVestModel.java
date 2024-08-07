package com.lying.wheelchairs.renderer.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.passive.CatEntity;

public class CatVestModel<T extends CatEntity> extends CatEntityModel<T>
{
	private static final String TAIL1 = "tail1";
	private static final String TAIL2 = "tail2";
	
	public CatVestModel(ModelPart modelPart)
	{
		super(modelPart);
	}
	
	public static ModelData getModelData(Dilation dilation)
	{
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().cuboid("main", -2.5f, -2.0f, -3.0f, 5.0f, 4.0f, 5.0f, dilation).cuboid(EntityModelPartNames.NOSE, -1.5f, -0.001f, -4.0f, 3, 2, 2, dilation, 0, 24).cuboid("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, dilation, 0, 10).cuboid("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, dilation, 6, 10), ModelTransform.pivot(0.0f, 15.0f, -9.0f));
		root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(20, 0).cuboid(-2.0f, 3.0f, -8.0f, 4.0f, 16.0f, 6.0f, dilation.add(0.15F)), ModelTransform.of(0.0f, 12.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
		root.addChild(TAIL1, ModelPartBuilder.create().uv(0, 15).cuboid(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, dilation), ModelTransform.of(0.0f, 15.0f, 8.0f, 0.9f, 0.0f, 0.0f));
		root.addChild(TAIL2, ModelPartBuilder.create().uv(4, 15).cuboid(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, new Dilation(-0.02F)), ModelTransform.pivot(0.0f, 20.0f, 14.0f));
		ModelPartBuilder hindLeg = ModelPartBuilder.create().uv(8, 13).cuboid(-1.0f, 0.0f, 1.0f, 2.0f, 6.0f, 2.0f, dilation);
		root.addChild(EntityModelPartNames.LEFT_HIND_LEG, hindLeg, ModelTransform.pivot(1.1f, 18.0f, 5.0f));
		root.addChild(EntityModelPartNames.RIGHT_HIND_LEG, hindLeg, ModelTransform.pivot(-1.1f, 18.0f, 5.0f));
		ModelPartBuilder foreLeg = ModelPartBuilder.create().uv(40, 0).cuboid(-1.0f, 0.0f, 0.0f, 2.0f, 10.0f, 2.0f, dilation);
		root.addChild(EntityModelPartNames.LEFT_FRONT_LEG, foreLeg, ModelTransform.pivot(1.2f, 14.1f, -5.0f));
		root.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, foreLeg, ModelTransform.pivot(-1.2f, 14.1f, -5.0f));
		return modelData;
	}
}
