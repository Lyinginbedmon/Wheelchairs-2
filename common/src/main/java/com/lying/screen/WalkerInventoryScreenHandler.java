package com.lying.screen;

import com.lying.entity.EntityWalker;
import com.lying.init.WHCScreenHandlerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class WalkerInventoryScreenHandler extends ScreenHandler
{
	private final Inventory inv;
	private final EntityWalker walker;
	
	public WalkerInventoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inv)
	{
		this(syncId, playerInventory, inv, null);
	}
	
	public WalkerInventoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inv, EntityWalker walkerIn)
	{
		super(WHCScreenHandlerTypes.WALKER_INVENTORY_HANDLER.get(), syncId);
		this.inv = inv;
		this.walker = walkerIn;
		
		// Chair inventory slots
		for(int k=0; k<3; ++k)
			for(int l=0; l < 5; ++l)
				this.addSlot(new Slot(inv, l + k * 5, 44 + l * 18, 18 + k * 18));
		
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
			else if(!insertItem(stackInSlot, 0, chairInvSize, false))
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
		return player.isAlive() && (walker == null || walker.isAlive() && walker.hasInventory() && player.distanceTo(walker) <= 4D);
	}

}
