package com.lying.neoforge.capability;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.lying.component.VestData;
import com.lying.item.ItemVest;
import com.lying.neoforge.ServerBus;
import com.lying.neoforge.WheelchairsNeoForge;
import com.lying.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class VestCapability extends VestData
{
	public static final Identifier IDENTIFIER = Reference.ModInfo.prefix( "vest_data");
	
	public boolean isDirty = false;
	
	public VestCapability(LivingEntity ownerIn)
	{
		super(ownerIn);
	}
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return WheelchairsNeoForge.VEST_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	public NbtCompound serializeNBT(RegistryWrapper.WrapperLookup lookup)
	{
		NbtCompound data = new NbtCompound();
		super.writeToNbt(data, lookup);
		return data;
	}
	
	public void deserializeNBT(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup)
	{
		super.readFromNbt(nbt, lookup);
	}
	
	public void setVest(ItemStack stack)
	{
		super.setVest(stack);
		isDirty = true;
	}
	
	public static void onLivingTick(final EntityTickEvent.Post event)
	{
		if(!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity e = (LivingEntity)event.getEntity();
		VestCapability cap = e.getCapability(WheelchairsNeoForge.VEST_DATA);
		if(cap == null)
			return;
		
		if(ItemVest.isValidMobForVest(e))
			cap.tick();
		
		if(cap.isDirty && !e.getWorld().isClient())
		{
			ServerBus.syncServiceAnimalToPlayers(e);
			cap.isDirty = false;
		}
	}
}
