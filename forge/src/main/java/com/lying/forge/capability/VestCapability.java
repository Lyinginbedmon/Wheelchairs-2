package com.lying.forge.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.component.VestData;
import com.lying.forge.WheelchairsForge;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;

public class VestCapability extends VestData implements ICapabilitySerializable<NbtCompound>
{
	public static final Identifier IDENTIFIER = new Identifier(Reference.ModInfo.MOD_ID, "vest_data");
	
	public VestCapability(LivingEntity ownerIn)
	{
		super(ownerIn);
	}
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return WheelchairsForge.VEST_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	public NbtCompound serializeNBT()
	{
		NbtCompound data = new NbtCompound();
		super.writeToNbt(data);
		return data;
	}
	
	public void deserializeNBT(NbtCompound nbt)
	{
		super.readFromNbt(nbt);
	}
	
	public static void onLivingTick(final LivingTickEvent event)
	{
		if(!ItemVest.isValidMobForVest(event.getEntity())) return;
		event.getEntity().getCapability(WheelchairsForge.VEST_DATA).resolve().get().tick();
	}
}
