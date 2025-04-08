package com.lying.fabric.component;

import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import com.lying.component.VestData;
import com.lying.fabric.init.WHCComponents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public class VestComponent extends VestData implements AutoSyncedComponent, ServerTickingComponent
{
	private ItemStack vestStack = ItemStack.EMPTY;
	
	public VestComponent(LivingEntity e) { super(e); }
	
	public void copyFrom(VestComponent other)
	{
		this.vestStack = other.vestStack.copy();
	}
	
	public boolean hasVest() { return !vestStack.isEmpty(); }
	
	public ItemStack get() { return vestStack.copy(); }
	
	public void setVest(ItemStack stack)
	{
		vestStack = stack;
		markDirty();
	}
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup)
	{
		if(tag.contains("Vest"))
			vestStack = ItemStack.fromNbtOrEmpty(lookup, tag.getCompound("Vest"));
	}
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup)
	{
		if(hasVest())
			tag.put("Vest", vestStack.toNbt(lookup));
	}
	
	public void markDirty() { WHCComponents.VEST_TRACKING.sync(owner); }
	
	public void serverTick()
	{
		super.tick();
	}
}
