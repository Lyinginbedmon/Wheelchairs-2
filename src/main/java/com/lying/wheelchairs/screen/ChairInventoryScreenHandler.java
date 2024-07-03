package com.lying.wheelchairs.screen;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCScreenHandlerTypes;
import com.lying.wheelchairs.init.WHCUpgrades;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ChairInventoryScreenHandler extends ScreenHandler
{
	private final Inventory inv;
	public final boolean hasStorage;
	public final boolean hasPlacer;
	
	public ChairInventoryScreenHandler(int syncId, PlayerInventory playerInventory, final EntityWheelchair chair)
	{
		super(WHCScreenHandlerTypes.WHEELCHAIR_INVENTORY_HANDLER, syncId);
		boolean noChair = chair == null;
		this.inv = noChair ? new SimpleInventory(16) : chair.getInventory();
		
		this.hasStorage = !noChair && chair.hasUpgrade(WHCUpgrades.STORAGE);
		this.hasPlacer = !noChair && chair.hasUpgrade(WHCUpgrades.PLACER);
		
		// (Optional) Placer slot
		this.addSlot(new Slot(inv, 0, 143, 36)
			{
				public boolean isEnabled() { return chair.hasUpgrade(WHCUpgrades.PLACER); }
				
				public boolean canInsert(ItemStack stack)
				{
					return stack.getItem() instanceof BlockItem;
				}
				
				
			});
		
		// (Optional) Main storage slots
		for(int k=0; k<3; ++k)
			for(int l=0; l < 5; ++l)
				this.addSlot(new Slot(inv, 1 + l + k * 5, 44 + l * 18, 18 + k * 18)
					{
						public boolean isEnabled() { return chair.hasUpgrade(WHCUpgrades.STORAGE); }
					});
		
		// Player inventory slots
		for(int k=0; k<3; ++k)
			for(int l=0; l<9; ++l)
				this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, 102 + (k - 1) * 18));
		
		// Player hotbar
		for(int k=0; k<9; ++k)
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
	}
	
	public ItemStack quickMove(PlayerEntity player, int slotIndex)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if(slot != null && slot.hasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();
			int chairInvSize = this.inv.size();
			if(slotIndex < chairInvSize)
			{
				if(!insertItem(stackInSlot, chairInvSize, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!insertItem(stackInSlot, 1, chairInvSize, false))
			{
				int invStart = chairInvSize;
				int hotbarStart = invStart + 27;
				int hotbarEnd = hotbarStart + 9;
				if(slotIndex >= hotbarStart && slotIndex < hotbarEnd ? 
						!insertItem(stackInSlot, invStart, hotbarStart, false) : 
						(slotIndex >= invStart && slotIndex < hotbarStart ? 
							!insertItem(stackInSlot, hotbarStart, hotbarEnd, false) : 
							!insertItem(stackInSlot, hotbarStart, hotbarStart, false)))
					return ItemStack.EMPTY;
				
				return ItemStack.EMPTY;
			}
			
			if(stackInSlot.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
		}
		
		return stack;
	}
	
	public boolean canUse(PlayerEntity player)
	{
		return player.hasVehicle() && player.isAlive() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && ((EntityWheelchair)player.getVehicle()).hasInventory();
	}

}
