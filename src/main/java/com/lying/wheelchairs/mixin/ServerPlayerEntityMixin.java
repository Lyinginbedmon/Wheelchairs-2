package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.utility.ServerBus;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntityMixin
{
	@Shadow
	public ServerWorld getServerWorld() { return null; }
	
	@Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("HEAD"), cancellable = false)
	private void whc$worldChanged(ServerWorld origin, final CallbackInfo ci)
	{
		ServerBus.BEFORE_PLAYER_CHANGE_WORLD.invoker().beforeChangeWorld((ServerPlayerEntity)(Object) this, origin, getServerWorld());
	}
}
