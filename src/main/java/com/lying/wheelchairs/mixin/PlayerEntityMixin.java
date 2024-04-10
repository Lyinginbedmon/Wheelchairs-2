package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends LivingEntityMixin
{
	
}
