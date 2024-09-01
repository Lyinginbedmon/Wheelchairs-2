package com.lying.fabric.component;

import com.lying.component.VestData;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class VestComponent extends VestData implements AutoSyncedComponent, ServerTickingComponent
{
	public VestComponent(LivingEntity ownerIn)
	{
		super(ownerIn);
	}
	
	public void readFromNbt(NbtCompound tag)
	{
		super.readFromNbt(tag);
	}
	
	public void writeToNbt(NbtCompound tag)
	{
		super.writeToNbt(tag);
	}
	
	public void serverTick()
	{
		super.tick();
	}
}
