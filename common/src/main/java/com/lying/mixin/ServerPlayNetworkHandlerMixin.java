package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.utility.Chairspace;

import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin
{
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(method = "onTeleportConfirm(Lnet/minecraft/network/packet/c2s/play/TeleportConfirmC2SPacket;)V", at = @At("TAIL"))
	private void whc$onTeleportConfirm(TeleportConfirmC2SPacket packet, final CallbackInfo ci)
	{
		Chairspace chairs = Chairspace.getChairspace(this.player.getServer());
		if(chairs.hasChairFor(this.player.getUuid()))
			chairs.tryRespawnChair(this.player.getUuid(), player);
	}
}
