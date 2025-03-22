package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.client.event.RenderEvents;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.resource.ResourceManager;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
	@Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("TAIL"))
	private void vt$onReload(ResourceManager manager, final CallbackInfo ci)
	{
		RenderEvents.ADD_FEATURE_RENDERERS_EVENT.invoker().append((EntityRenderDispatcher)(Object)this);
	}
}
