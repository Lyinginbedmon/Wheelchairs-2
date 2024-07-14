package com.lying.wheelchairs.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCSoundEvents;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
		NbtCompound data = stack.getOrCreateNbt();
		data.put("Handle", handle.writeNbt(new NbtCompound()));
		stack.setNbt(data);
	}
	
	public ItemStack getHandle(ItemStack stack)
	{
		if(stack.getItem() instanceof ItemCane && stack.hasNbt())
		{
			NbtCompound data = stack.getNbt();
			if(data.contains("Handle", NbtElement.COMPOUND_TYPE))
				return ItemStack.fromNbt(data.getCompound("Handle"));
		}
		return WHCItems.HANDLE_OAK.getDefaultStack().copy();
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
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
			return TypedActionResult.success(sword);
		}
		return TypedActionResult.pass(heldStack);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!stack.hasNbt())
			return;
		
		tooltip.add(Text.translatable("gui.wheelchairs.cane.handle", getHandle(stack).getName()));
		
		ItemStack sword = getSword(stack);
		if(!sword.isEmpty())
			tooltip.add(Text.translatable("gui.wheelchairs.cane.sword", sword.getName()));
	}
	
	public static ItemStack setSword(ItemStack cane, ItemStack sword)
	{
		NbtCompound data = cane.getOrCreateNbt();
		if(sword.isEmpty())
			data.remove("Contains");
		else
			data.put("Contains", sword.writeNbt(new NbtCompound()));
		cane.setNbt(data);
		return cane;
	}
	
	public static ItemStack getSword(ItemStack cane)
	{
		if(cane.getItem() instanceof ItemCane && cane.hasNbt())
		{
			NbtCompound data = cane.getNbt();
			if(data.contains("Contains", NbtElement.COMPOUND_TYPE))
				return ItemStack.fromNbt(data.getCompound("Contains"));
		}
		return ItemStack.EMPTY;
	}
}
