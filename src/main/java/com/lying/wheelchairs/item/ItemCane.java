package com.lying.wheelchairs.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.init.WHCItems;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
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
		ItemStack handle = WHCItems.HANDLE_OAK.getDefaultStack().copy();
		if(stack.getItem() instanceof ItemCane && stack.hasNbt())
		{
			NbtCompound data = stack.getNbt();
			if(data.contains("Handle", NbtElement.COMPOUND_TYPE))
				handle = ItemStack.fromNbt(data.getCompound("Handle"));
		}
		return handle;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!stack.hasNbt())
			return;
		
		tooltip.add(Text.translatable("gui.wheelchairs.cane.handle", getHandle(stack).getName()));
	}
}
