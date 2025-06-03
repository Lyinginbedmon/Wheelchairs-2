package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.lying.entity.IHeldItemRenderer;
import com.lying.entity.IServiceVestHolder;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IServiceVestHolder, IHeldItemRenderer
{
	@Unique
	private ItemStack serviceVest = ItemStack.EMPTY;
	
	@Unique
	private ItemStack heldItemRight = ItemStack.EMPTY, heldItemLeft = ItemStack.EMPTY;
	
	public ItemStack getVest() { return serviceVest; }
	
	public void setVest(ItemStack stack) { serviceVest = stack; }
	
	public void setHandItem(ItemStack stack, Arm hand)
	{
		if(hand == Arm.RIGHT)
			heldItemRight = stack;
		else
			heldItemLeft = stack;
	}
	
	public ItemStack getHeldItem(Arm hand) { return hand == Arm.RIGHT ? heldItemRight : heldItemLeft; }
	
	public void setHeldItem(ItemStack stack, Arm hand)
	{
		if(hand == Arm.RIGHT)
			heldItemRight = stack;
		else
			heldItemLeft = stack;
	}
}
