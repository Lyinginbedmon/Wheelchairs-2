package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.lying.entity.IHeldItemRenderer;
import com.lying.entity.IParentEntity;
import com.lying.entity.IServiceVestHolder;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IServiceVestHolder, IHeldItemRenderer, IParentEntity
{
	@Unique
	private boolean isActiveParent = false;
	
	@Unique
	private ItemStack serviceVest = ItemStack.EMPTY;
	
	@Unique
	private ItemStack heldItemRight = ItemStack.EMPTY, heldItemLeft = ItemStack.EMPTY;
	
	public ItemStack getVest() { return serviceVest; }
	
	public void setVest(ItemStack stack) { serviceVest = stack; }
	
	public ItemStack getHeldItem(Arm arm)
	{
		switch(arm)
		{
			case RIGHT:
				return heldItemRight;
			case LEFT:
				return heldItemLeft;
		}
		return ItemStack.EMPTY;
	}
	
	public void setHeldItem(ItemStack stack, Arm arm)
	{
		switch(arm)
		{
			case RIGHT:
				heldItemRight = stack;
				break;
			case LEFT:
				heldItemLeft = stack;
				break;
		}
	}
	
	public boolean hasParentedEntities() { return isActiveParent; }
	
	public void setHasParentedEntities(boolean val) { isActiveParent = val; }
}
