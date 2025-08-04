package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.entity.IHeldItemRenderer;
import com.lying.entity.IParentEntity;
import com.lying.item.CaneItem;
import com.lying.item.CrutchItem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin extends BipedEntityModelMixin
{
	private static boolean shouldModify(EntityPose currentPose, ArmPose rightArmPose, ArmPose leftArmPose)
	{
		return
				(currentPose == EntityPose.STANDING || currentPose == EntityPose.CROUCHING) &&
				!(rightArmPose.isTwoHanded() || leftArmPose.isTwoHanded());
	}
	
	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("HEAD"), cancellable = true)
	public void whc$setAnglesHead(PlayerEntityRenderState state, final CallbackInfo ci)
	{
		if(!shouldModify(state.pose, state.rightArmPose, state.leftArmPose))
			return;
		
		animHandlingWalkers(state);
	}
	
	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"), cancellable = true)
	public void whc$setAnglesTail(PlayerEntityRenderState state, final CallbackInfo ci)
	{
		if(!shouldModify(state.pose, state.rightArmPose, state.leftArmPose))
			return;
		
		handleAnimTweaks(
				state, 
				state.age, 
				state.isInPose(EntityPose.CROUCHING), 
				state.mainArm == Arm.RIGHT, 
				getLeg(state.mainArm), 
				getLeg(state.mainArm.getOpposite()));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends PlayerEntityRenderState & IHeldItemRenderer> void handleAnimTweaks(PlayerEntityRenderState state, float age, boolean isCrouching, boolean rightHanded, ModelPart mainLeg, ModelPart offLeg)
	{
		animHandlingCrutches((T)state, isCrouching, rightHanded, mainLeg, offLeg);
		animHandlingCanes((T)state, age, rightHanded, isCrouching);
	}
	
	private <T extends PlayerEntityRenderState & IHeldItemRenderer> void animHandlingCrutches(T entity, boolean isCrouching, boolean rightHanded, ModelPart mainLeg, ModelPart offLeg)
	{
		boolean isPair = isHoldingPair(entity);
		double amount = isCrouching ? 25D : entity.hasVehicle ? 15D : 10D;
		float roll = (float)Math.toRadians(amount);
		if(entity.hasVehicle)
		{
			if(isCrutch(entity.getHeldItem(entity.mainArm)))
				getArm(entity.mainArm).roll += roll * (rightHanded ? 1F : -1F);
			
			if(isCrutch(entity.getHeldItem(entity.mainArm.getOpposite())))
				getArm(entity.mainArm.getOpposite()).roll += roll * (rightHanded ? -1F : 1F);
			
			return;
		}
		
		if(entity.handSwingProgress == 0F || entity.mainArm != Arm.RIGHT)
			correctCrutchPose(entity.getHeldItem(Arm.RIGHT), getArm(Arm.RIGHT), mainLeg, offLeg, rightHanded, isPair);
		if(entity.handSwingProgress == 0F || entity.mainArm != Arm.LEFT)
			correctCrutchPose(entity.getHeldItem(Arm.LEFT), getArm(Arm.LEFT), mainLeg, offLeg, !rightHanded, isPair);
		
		if(isPair)
		{
			getArm(Arm.RIGHT).roll = roll;
			getArm(Arm.LEFT).roll = -roll;
			
			offLeg.pitch = (Math.abs(offLeg.pitch) * 0.3F) + (float)Math.toRadians(40D);
		}
		else if(isCrouching)
		{
			if(isCrutch(entity.getHeldItem(entity.mainArm)))
				getArm(entity.mainArm).roll = roll * (rightHanded ? 1F : -1F);
			
			if(isCrutch(entity.getHeldItem(entity.mainArm.getOpposite())))
				getArm(entity.mainArm.getOpposite()).roll = roll * (rightHanded ? -1F : 1F);
		}
	}
	
	private <T extends PlayerEntityRenderState & IHeldItemRenderer> void animHandlingCanes(T entity, float ageInTicks, boolean isRightHanded, boolean isCrouching)
	{
		if(entity.hasVehicle) return;
		
		if(isCane(entity.getHeldItem(entity.mainArm)))
		{
			ModelPart mainArm = getArm(entity.mainArm);
			CrossbowPosing.swingArm(mainArm, ageInTicks, isRightHanded ? -1F : 1F);
			if(isCrouching)
				mainArm.pitch -= Math.toRadians(30D);
		}
		
		if(isCane(entity.getHeldItem(entity.mainArm.getOpposite())))
		{
			ModelPart offArm = getArm(entity.mainArm.getOpposite());
			CrossbowPosing.swingArm(offArm, ageInTicks, isRightHanded ? 1F : -1F);
			if(isCrouching)
				offArm.pitch -= Math.toRadians(30D);
		}
	}
	
	private void animHandlingWalkers(PlayerEntityRenderState state)
	{
		if(((IParentEntity)state).hasParentedEntities())
			return;
		
		if(state.rightArmPose == ArmPose.EMPTY)
			state.rightArmPose = ArmPose.ITEM;
		
		if(state.leftArmPose == ArmPose.EMPTY)
			state.leftArmPose = ArmPose.ITEM;
	}
	
	private ModelPart getLeg(Arm arm) { return arm == Arm.RIGHT ? rightLeg : leftLeg; }
	
	private static boolean isCrutch(ItemStack stack) { return !stack.isEmpty() && stack.getItem() instanceof CrutchItem; }
	
	private static boolean isCane(ItemStack stack) { return !stack.isEmpty() && stack.getItem() instanceof CaneItem; }
	
	private static boolean isHoldingPair(IHeldItemRenderer entity)
	{
		return isCrutch(entity.getHeldItem(Arm.RIGHT)) && isCrutch(entity.getHeldItem(Arm.LEFT));
	}
	
	private static void correctCrutchPose(ItemStack crutch, ModelPart arm, ModelPart legMain, ModelPart legOff, boolean isMain, boolean isPair)
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
