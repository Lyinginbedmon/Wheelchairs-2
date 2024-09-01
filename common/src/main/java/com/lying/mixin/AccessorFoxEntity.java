package com.lying.mixin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.FoxEntity;

@Mixin(FoxEntity.class)
public interface AccessorFoxEntity
{
	@Accessor("OWNER")
	public TrackedData<Optional<UUID>> OWNER_UUID();
}
