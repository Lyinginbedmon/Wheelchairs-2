package com.lying.wheelchairs.mixin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.passive.FoxEntity;

@Mixin(FoxEntity.class)
public class FoxEntityMixin extends EntityMixin
{
	public Optional<UUID> getOwnerID() { return getDataTracker().get(((AccessorFoxEntity)(FoxEntity)(Object)this).OWNER_UUID()); }
}
