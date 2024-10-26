package com.lying.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.entity.IParentedEntity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin
{
	@SuppressWarnings("rawtypes")
	@Inject(method = "tickEntity(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void whc$tickEntity(Consumer tickConsumer, Entity entity, final CallbackInfo ci)
	{
		if(entity.getWorld().isClient() && entity instanceof IParentedEntity && ((IParentedEntity)entity).hasParent())
			ci.cancel();
	}
}
