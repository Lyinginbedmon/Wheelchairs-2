package com.lying.component;

import com.lying.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public abstract class VestData
{
	protected final LivingEntity owner;
	private ItemStack vest = ItemStack.EMPTY;
	
	public VestData(LivingEntity ownerIn) { this.owner = ownerIn; }
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup)
	{
		vest = ItemStack.EMPTY;
		if(tag.contains("Vest", NbtElement.COMPOUND_TYPE))
			vest = ItemStack.fromNbt(lookup, tag.getCompound("Vest")).orElse(ItemStack.EMPTY);
	}
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup)
	{
		if(!vest.isEmpty())
			tag.put("Vest", vest.toNbt(lookup));
	}
	
	public boolean hasVest() { return !vest.isEmpty(); }
	
	public ItemStack get() { return vest; }
	
	public void setVest(ItemStack stack) { vest = stack.copy(); }
	
	public void tick()
	{
		if(!hasVest() || !(owner instanceof LivingEntity)) return;
		
		if(owner.getHealth() < owner.getMaxHealth() && owner.age%Reference.Values.TICKS_PER_MINUTE == 0)
			owner.heal(1F);
	}
}
