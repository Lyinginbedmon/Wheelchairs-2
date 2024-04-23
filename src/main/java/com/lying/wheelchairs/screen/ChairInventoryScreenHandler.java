package com.lying.wheelchairs.screen;

import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCScreenHandlerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ChairInventoryScreenHandler extends ScreenHandler
{
	public ChairInventoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inv)
	{
		super(WHCScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, syncId);
		
		for(int k =0; k<3; ++k)
			for(int l = 0; l < 5; ++l)
				this.addSlot(new Slot(inv, l + k * 5, 80 + l * 18, 18 + k * 18));
		
		// Player inventory slots
		for(int k=0; k<3; ++k)
			for(int l=0; l<9; ++l)
				this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, 110 + (k - 1) * 18));
		
		for(int k=0; k<9; ++k)
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 150));
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean canUse(PlayerEntity player) { return player.hasVehicle() && player.isAlive() && player.getVehicle().getType() == WHCEntityTypes.WHEELCHAIR && ((EntityWheelchair)player.getVehicle()).hasInventory(); }

}
