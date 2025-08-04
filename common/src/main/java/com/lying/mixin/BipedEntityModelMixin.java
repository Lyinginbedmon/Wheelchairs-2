package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Arm;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin extends EntityModelMixin
{
	@Shadow
	public ModelPart rightLeg;
	
	@Shadow
	public ModelPart leftLeg;
	
	@Shadow
	public ModelPart getArm(Arm armIn) { return null; }
}
