package com.lying.wheelchairs.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.wheelchairs.data.WHCItemTags;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin
{
	@Shadow
	public PlayerEntity player;
	
	@Shadow
	private List<DefaultedList<ItemStack>> combinedInventory;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject(method = "dropAll()V", at = @At("HEAD"), cancellable = true)
	private void whc$dropAll(final CallbackInfo ci)
	{
		ci.cancel();
		
		// Prevents any IPreservedItem from being dropped on death
		for(List list : this.combinedInventory)
			for(int i=0; i<list.size(); ++i)
			{
				ItemStack stack = (ItemStack)list.get(i);
				if(stack.isEmpty() || stack.isIn(WHCItemTags.PRESERVED))
					continue;
				this.player.dropItem(stack, true, false);
				list.set(i, ItemStack.EMPTY);
			}
	}
}
