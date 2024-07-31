package com.lying.wheelchairs;

import com.lying.wheelchairs.init.WHCComponents;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class VestComponent implements AutoSyncedComponent
{
	private ItemStack vestStack = ItemStack.EMPTY;
	
	private final Entity ent;
	
	public VestComponent(Entity e) { ent = e; }
	
	public void copyFrom(VestComponent other)
	{
		this.vestStack = other.vestStack.copy();
	}
	
	public ItemStack get() { return vestStack.copy(); }
	
	public void setVest(ItemStack stack)
	{
		vestStack = stack;
		markDirty();
	}
	
	public void readFromNbt(NbtCompound tag)
	{
		vestStack = ItemStack.fromNbt(tag.getCompound("Vest"));
	}
	
	public void writeToNbt(NbtCompound tag)
	{
		tag.put("Vest", vestStack.writeNbt(new NbtCompound()));
	}
	
	public void markDirty() { WHCComponents.VEST_TRACKING.sync(ent); }
}
