package com.lying.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(EntityRenderDispatcher.class)
public interface AccessorEntityRenderDispatcher
{
	@Accessor("renderers")
	public Map<EntityType<?>, EntityRenderer<?,?>> getRenderers();
	
	@Accessor("modelRenderers")
	public Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity, ?>> getModelRenderers();
}
