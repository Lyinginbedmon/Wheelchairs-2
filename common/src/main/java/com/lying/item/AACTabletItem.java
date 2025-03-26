package com.lying.item;

import com.lying.Wheelchairs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class AACTabletItem extends Item
{
	public AACTabletItem(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult use(World world, PlayerEntity user, Hand hand)
	{
		if(world.isClient())
			Wheelchairs.openAACScreen(user, user.getStackInHand(hand));
		return ActionResult.SUCCESS;
	}
}
