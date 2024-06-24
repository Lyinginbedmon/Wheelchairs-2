package com.lying.wheelchairs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.network.ParentedEntityInputPacket;
import com.lying.wheelchairs.network.ParentedEntityMovePacket;

import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntityMixin
{
	@Shadow
	public Input input;
	
	@Inject(method = "tick()V", at = @At("HEAD"))
	public void tick(final CallbackInfo ci)
	{
		ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
		ParentedEntityInputPacket.send(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking);
		ParentedEntityMovePacket.send(player);
	}
}
