package com.lying.wheelchairs;

import com.lying.wheelchairs.init.WHCComponents;
import com.lying.wheelchairs.reference.Reference;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class VestComponent implements AutoSyncedComponent, ServerTickingComponent
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
	
	public void serverTick()
	{
		if(!vestStack.isEmpty() && ent.isAlive() && ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			if(living.getHealth() < living.getMaxHealth() && living.age%Reference.Values.TICKS_PER_MINUTE == 0)
				living.heal(1F);
		}
	}
}
