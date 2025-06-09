package com.lying.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.init.WHCDataComponentTypes;
import com.lying.item.CaneItem;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin
{
	@Shadow
	private void update(
			ItemRenderState renderState, 
			ItemStack stack, 
			ModelTransformationMode transformationMode, 
			@Nullable World world, 
			@Nullable LivingEntity entity, 
			int seed
			) { }
	
	@Inject(method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At("HEAD"))
	private void whc$update(
			ItemRenderState renderState, 
			ItemStack stack, 
			ModelTransformationMode transformationMode, 
			@Nullable World world, 
			@Nullable LivingEntity entity, 
			int seed,
			final CallbackInfo ci)
	{
		if(stack.getItem() instanceof CaneItem && shouldRenderHandle(transformationMode))
			stack.get(WHCDataComponentTypes.HANDLE.get()).contents().ifPresent(handle -> update(renderState, handle, transformationMode, world, entity, seed));
	}
	
	private static boolean shouldRenderHandle(ModelTransformationMode renderMode)
	{
		switch(renderMode)
		{
			case FIRST_PERSON_LEFT_HAND:
			case FIRST_PERSON_RIGHT_HAND:
			case THIRD_PERSON_LEFT_HAND:
			case THIRD_PERSON_RIGHT_HAND:
				return true;
			case FIXED:
			case GROUND:
			case GUI:
			case HEAD:
			case NONE:
			default:
				return false;
		}
	}
}
