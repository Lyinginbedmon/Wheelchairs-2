package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.lying.entity.IServiceVestHolder;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IServiceVestHolder
{
	@Unique
	private ItemStack serviceVest = ItemStack.EMPTY;
	
	public ItemStack getVest() { return serviceVest; }
	
	public void setVest(ItemStack stack) { serviceVest = stack; }
}
