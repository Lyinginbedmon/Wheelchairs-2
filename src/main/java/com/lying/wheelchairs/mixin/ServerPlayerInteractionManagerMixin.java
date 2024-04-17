package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.utility.ServerBus;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin
{
	@Shadow
	protected ServerPlayerEntity player;
	
	@Inject(method = "setGameMode(Lnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;)V", at = @At("RETURN"), cancellable = false)
	private void whc$setGameMode(GameMode gameMode, GameMode previousGameMode, final CallbackInfo ci)
	{
		if(player == null || player.getWorld() == null)
			return;
		
		ServerBus.AFTER_PLAYER_CHANGE_GAME_MODE.invoker().afterChangeGameMode(player, gameMode);
	}
}
