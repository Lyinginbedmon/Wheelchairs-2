package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.entity.model.EntityModel;

@Mixin(EntityModel.class)
public class EntityModelMixin
{
	@Shadow
	public float handSwingProgress;
}
