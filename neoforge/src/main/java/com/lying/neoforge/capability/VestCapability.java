package com.lying.neoforge.capability;

import com.lying.component.VestData;
import com.lying.init.WHCItems;
import com.lying.item.VestItem;
import com.lying.neoforge.ServerBus;
import com.lying.neoforge.WheelchairsNeoForge;
import com.lying.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.items.IItemHandler;

public class VestCapability extends VestData implements IItemHandler
{
	public static final Identifier IDENTIFIER = Reference.ModInfo.prefix( "vest_data");
	
	public boolean isDirty = false;
	
	public VestCapability(LivingEntity ownerIn)
	{
		super(ownerIn);
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
	
	public int getSlots() { return 1; }
	
	public int getSlotLimit(int slot) { return 1; }
	
	public boolean isItemValid(int slot, ItemStack stack) { return stack.isOf(WHCItems.VEST.get()); }
	
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		super.setVest(stack.split(1));
		isDirty = true;
		return stack;
	}
	
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(amount <= 0)
			return ItemStack.EMPTY;
		
		ItemStack stackInSlot = get().copy();
		setVest(ItemStack.EMPTY);
		isDirty = true;
		return stackInSlot;
	}
	
	public ItemStack getStackInSlot(int slot) { return get(); }
	
	public void setVest(ItemStack stack)
	{
		super.setVest(stack);
		isDirty = true;
	}
	
	public static void onLivingTick(final EntityTickEvent.Post event)
	{
		if(!(event.getEntity() instanceof LivingEntity) || !VestItem.isValidMobForVest(event.getEntity()))
			return;
		
		LivingEntity e = (LivingEntity)event.getEntity();
		VestCapability cap = e.getCapability(WheelchairsNeoForge.VEST_DATA);
		if(cap == null)
			return;
		
		cap.tick();
		
		if(cap.isDirty && !e.getWorld().isClient())
		{
			ServerBus.syncServiceAnimalToPlayers(e);
			cap.isDirty = false;
		}
	}
}
