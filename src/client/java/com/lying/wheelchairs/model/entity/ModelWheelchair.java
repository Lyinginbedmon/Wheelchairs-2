package com.lying.wheelchairs.model.entity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

public class ModelWheelchair<T extends Entity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	
	public ModelWheelchair(ModelPart data)
	{
		this.root = data.getChild(EntityModelPartNames.ROOT);
	}
	
	public static TexturedModelData getMainModel()
	{
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();
		
		partdefinition.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create()
			.uv(0, 0).cuboid(-5.0F, -1.0F, -6.0F, 10.0F, 4.0F, 8.0F, new Dilation(0.0F))
			.uv(0, 12).cuboid(-4.5F, -5.0F, 2.0F, 9.0F, 7.0F, 1.0F, new Dilation(0.0F))
			.uv(0, 21).cuboid(-7.0F, 3.5F, -0.5F, 14.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 12.0F, 0.0F));
		
		return TexturedModelData.of(meshdefinition, 64, 32);
	}
	
	public ModelPart getPart() { return this.root; }
	
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) { }
}
