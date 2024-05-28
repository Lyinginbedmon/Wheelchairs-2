package com.lying.wheelchairs.renderer.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class WheelchairElytraModel<T extends LivingEntity> extends SinglePartEntityModel<T>
{
	private static final String PIVOT_PART = "pivot";
	private static final float BASE_TILT = (float)Math.toRadians(15D);
	private final ModelPart pivot;
	private final ModelPart wingRight;
	private final ModelPart wingLeft;
	
	public WheelchairElytraModel(ModelPart root)
	{
		this.pivot = root.getChild(PIVOT_PART);
		this.wingRight = this.pivot.getChild(EntityModelPartNames.RIGHT_WING);
		this.wingLeft = this.pivot.getChild(EntityModelPartNames.LEFT_WING);
	}
	
	public static TexturedModelData createBodyLayer()
	{
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();
		
		ModelPartData pivot = partdefinition.addChild(PIVOT_PART, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 5.0F, 0.0F));
		pivot.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(22, 0).mirrored().cuboid(-2.0F, -1.0F, -1.0F, 10.0F, 20.0F, 2.0F, Dilation.NONE), ModelTransform.of(-2.0F, 0.0F, 0.0F, BASE_TILT, 0.0F, BASE_TILT));
		pivot.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(22, 0).cuboid(-8.0F, -1.0F, -1.0F, 10.0F, 20.0F, 2.0F, Dilation.NONE), ModelTransform.of(2.0F, 0.0F, 0.0F, BASE_TILT, 0.0F, -BASE_TILT));
		
		return TexturedModelData.of(meshdefinition, 64, 32);
	}
	
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		float wingPitch = BASE_TILT;
		float wingRoll = -BASE_TILT;
		float wingYaw = 0.0f;
		if(entity.isFallFlying())
		{
			float o = 1.0f;
			Vec3d vec3d = entity.getVelocity();
			if (vec3d.y < 0.0)
			{
				Vec3d vec3d2 = vec3d.normalize();
				o = 1.0f - (float)Math.pow(-vec3d2.y, 1.5);
			}
			wingPitch = o * 0.34906584f + (1.0f - o) * wingPitch;
			wingPitch += (float)Math.toRadians(90D);
			
			wingRoll = o * -1.5707964f + (1.0f - o) * wingRoll;
		}
		else if(entity.isInSneakingPose())
		{
			wingPitch = (float)Math.toRadians(40D);
			wingRoll = -(float)Math.toRadians(45D);
			wingYaw = (float)Math.toRadians(5D);
		}
		
		this.pivot.pitch = wingPitch;
		
		this.wingLeft.yaw = wingYaw;
		this.wingRight.roll = -this.wingLeft.roll;
		this.wingLeft.roll = wingRoll;
		this.wingRight.yaw = -this.wingLeft.yaw;
	}
	
	public ModelPart getPart()
	{
		return pivot;
	}
}