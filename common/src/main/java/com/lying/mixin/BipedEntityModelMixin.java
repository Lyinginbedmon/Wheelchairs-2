package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.EntityPose;
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
	
	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V", at = @At("HEAD"), cancellable = true)
	public void whc$setAnglesHead(EntityRenderState stateIn, final CallbackInfo ci)
	{
		if(stateIn instanceof PlayerEntityRenderState)
		{
			PlayerEntityRenderState state = (PlayerEntityRenderState)stateIn;
			if(!(state.isInPose(EntityPose.STANDING) || state.isInPose(EntityPose.CROUCHING)) || (state.rightArmPose.isTwoHanded() || state.leftArmPose.isTwoHanded()))
				return;
			
			animHandlingWalkers(state);
		}
	}
	
	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V", at = @At("TAIL"), cancellable = true)
	public void whc$setAnglesTail(EntityRenderState stateIn, final CallbackInfo ci)
	{
		if(stateIn instanceof PlayerEntityRenderState)
		{
			PlayerEntityRenderState state = (PlayerEntityRenderState)stateIn;
			if(!(state.isInPose(EntityPose.STANDING) || state.isInPose(EntityPose.CROUCHING)) || (state.rightArmPose.isTwoHanded() || state.leftArmPose.isTwoHanded()))
				return;
			
			ModelPart mainLeg = getLeg(state.mainArm);
			ModelPart offLeg = getLeg(state.mainArm.getOpposite());
			boolean rightHanded = state.mainArm == Arm.RIGHT;
			boolean isCrouching = state.isInPose(EntityPose.CROUCHING);
			
			animHandlingCrutches(state, isCrouching, rightHanded, mainLeg, offLeg);
			animHandlingCanes(state, state.age, rightHanded, isCrouching);
		}
	}
	
	private void animHandlingCrutches(PlayerEntityRenderState entity, boolean isCrouching, boolean rightHanded, ModelPart mainLeg, ModelPart offLeg)
	{
		boolean isPair = isHoldingPair(entity);
		double amount = isCrouching ? 25D : entity.hasVehicle ? 15D : 10D;
		float roll = (float)Math.toRadians(amount);
		if(entity.hasVehicle)
		{
			if(isCrutch(entity.getMainHandItemState()))
				getArm(entity.mainArm).roll += roll * (rightHanded ? 1F : -1F);
			
			if(isCrutch(getOffHandItemState(entity)))
				getArm(entity.mainArm.getOpposite()).roll += roll * (rightHanded ? -1F : 1F);
			
			return;
		}
		
		if(entity.handSwingProgress == 0F || entity.mainArm != Arm.RIGHT)
			correctCrutchPose(entity.mainArm == Arm.RIGHT ? entity.getMainHandItemState() : getOffHandItemState(entity), getArm(Arm.RIGHT), mainLeg, offLeg, rightHanded, isPair);
		if(entity.handSwingProgress == 0F || entity.mainArm != Arm.LEFT)
			correctCrutchPose(entity.mainArm == Arm.LEFT ? entity.getMainHandItemState() : getOffHandItemState(entity), getArm(Arm.LEFT), mainLeg, offLeg, !rightHanded, isPair);
		
		if(isPair)
		{
			getArm(Arm.RIGHT).roll = roll;
			getArm(Arm.LEFT).roll = -roll;
			
			offLeg.pitch = (Math.abs(offLeg.pitch) * 0.3F) + (float)Math.toRadians(40D);
		}
		else if(isCrouching)
		{
			if(isCrutch(entity.getMainHandItemState()))
				getArm(entity.mainArm).roll = roll * (rightHanded ? 1F : -1F);
			
			if(isCrutch(getOffHandItemState(entity)))
				getArm(entity.mainArm.getOpposite()).roll = roll * (rightHanded ? -1F : 1F);
		}
	}
	
	private void animHandlingCanes(PlayerEntityRenderState entity, float ageInTicks, boolean isRightHanded, boolean isCrouching)
	{
		if(entity.hasVehicle) return;
		
		if(isCane(entity.getMainHandItemState()))
		{
			ModelPart mainArm = getArm(entity.mainArm);
			CrossbowPosing.swingArm(mainArm, ageInTicks, isRightHanded ? -1F : 1F);
			if(isCrouching)
				mainArm.pitch -= Math.toRadians(30D);
		}
		
		if(isCane(getOffHandItemState(entity)))
		{
			ModelPart offArm = getArm(entity.mainArm.getOpposite());
			CrossbowPosing.swingArm(offArm, ageInTicks, isRightHanded ? 1F : -1F);
			if(isCrouching)
				offArm.pitch -= Math.toRadians(30D);
		}
	}
	
	private ItemRenderState getOffHandItemState(PlayerEntityRenderState entity)
	{
		return entity.mainArm == Arm.RIGHT ? entity.leftHandItemState : entity.rightHandItemState;
	}
	
	private void animHandlingWalkers(PlayerEntityRenderState state)
	{
		// FIXME Detect parented entities during rendering
//		if(IParentedEntity.getParentedEntitiesOf(entity).isEmpty())
//			return;
		
		if(state.rightArmPose == ArmPose.EMPTY)
			state.rightArmPose = ArmPose.ITEM;
		
		if(state.leftArmPose == ArmPose.EMPTY)
			state.leftArmPose = ArmPose.ITEM;
	}
	
	private ModelPart getLeg(Arm arm) { return arm == Arm.RIGHT ? rightLeg : leftLeg; }
	
	// FIXME Detect held item type during rendering
	
	private static boolean isCrutch(ItemRenderState stack) { return false; }//!stack.isEmpty() && stack.getItem() instanceof ItemCrutch; }
	
	private static boolean isCane(ItemRenderState stack) { return false; }//!stack.isEmpty() && stack.getItem() instanceof ItemCane; }
	
	private static boolean isHoldingPair(PlayerEntityRenderState entity) { return isCrutch(entity.rightHandItemState) && isCrutch(entity.leftHandItemState); }
	
	private static void correctCrutchPose(ItemRenderState crutch, ModelPart arm, ModelPart legMain, ModelPart legOff, boolean isMain, boolean isPair)
	{
		if(!isCrutch(crutch))
			return;
		
		if(isPair)
			arm.pitch = legOff.pitch;
		else
			arm.pitch = isMain ? legMain.pitch : legOff.pitch;
		
		arm.pitch *= 0.5F;
		arm.yaw = 0F;
		arm.roll = 0F;
	}
}
