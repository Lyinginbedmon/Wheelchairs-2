package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.minecraft.entity.Entity;
import net.minecraft.world.tick.TickManager;

@Mixin(TickManager.class)
public class TickManagerMixinClient
{
	@Inject(method = "shouldSkipTick(Lnet/minecraft/entity/Entity;)Z", at = @At("TAIL"), cancellable = true)
	private void whc$shouldSkipTick(Entity entity, final CallbackInfoReturnable<Boolean> ci)
	{
		if(entity instanceof IParentedEntity && ((IParentedEntity)entity).hasParent())
			ci.setReturnValue(true);
	}
}
