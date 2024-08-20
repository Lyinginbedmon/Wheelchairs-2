package com.lying.wheelchairs.item;

import com.lying.wheelchairs.Wheelchairs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemAACTablet extends Item
{
	public ItemAACTablet(Settings settings)
	{
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		if(world.isClient())
			Wheelchairs.openAACScreen(user, user.getStackInHand(hand));
		return TypedActionResult.success(user.getStackInHand(hand));
	}
}
