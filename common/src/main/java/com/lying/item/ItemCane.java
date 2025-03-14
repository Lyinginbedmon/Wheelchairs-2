package com.lying.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCSoundEvents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemCane extends Item
{
	public ItemCane(Settings settings)
	{
		super(settings);
	}
	
	public static ItemStack withHandle(Item cane, Item handle)
	{
		ItemStack defaultStack = cane.getDefaultStack();
		setHandle(defaultStack, handle.getDefaultStack());
		return defaultStack;
	}
	
	public static void setHandle(ItemStack stack, ItemStack handle)
	{
		stack.set(WHCDataComponentTypes.HANDLE.get(), handle.copy());
	}
	
	public ItemStack getHandle(ItemStack stack)
	{
		return stack.contains(WHCDataComponentTypes.HANDLE.get()) ? stack.get(WHCDataComponentTypes.HANDLE.get()) : WHCItems.HANDLE_OAK.get().getDefaultStack().copy();
	}
	
	public ActionResult use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack heldStack = user.getStackInHand(hand);
		ItemStack sword = getSword(heldStack);
		if(user.isSneaking() && !sword.isEmpty())
		{
			ItemStack cane = setSword(heldStack.copy(), ItemStack.EMPTY);
			Hand opposite = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
			if(user.getStackInHand(opposite).isEmpty())
				user.setStackInHand(opposite, cane);
			else
				user.getInventory().insertStack(cane);
			
			world.playSound(null, user.getX(), user.getY(), user.getZ(), WHCSoundEvents.SWORD_DRAW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
			return ActionResult.SUCCESS.withNewHandStack(sword);
		}
		return ActionResult.PASS;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		tooltip.add(Text.translatable("gui.wheelchairs.cane.handle", getHandle(stack).getName()));
		
		ItemStack sword = getSword(stack);
		if(!sword.isEmpty())
			tooltip.add(Text.translatable("gui.wheelchairs.cane.sword", sword.getName()));
	}
	
	public static ItemStack setSword(ItemStack cane, ItemStack sword)
	{
		cane.set(WHCDataComponentTypes.SWORD.get(), sword.copy());
		return cane;
	}
	
	public static ItemStack getSword(ItemStack cane)
	{
		return cane.contains(WHCDataComponentTypes.SWORD.get()) ? cane.get(WHCDataComponentTypes.SWORD.get()) : ItemStack.EMPTY;
	}
}
