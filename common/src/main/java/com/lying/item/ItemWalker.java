package com.lying.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.lying.entity.EntityWalker;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWalker extends EntityPlacerItem<EntityWalker> implements IBonusBlockItem
{
	public ItemWalker(Settings settings)
	{
		super(WHCEntityTypes.WALKER, settings);
	}
	
	public static ItemStack withWheels(Item chair, Item wheels)
	{
		ItemStack defaultStack = chair.getDefaultStack();
		setWheels(defaultStack, wheels.getDefaultStack(), wheels.getDefaultStack());
		return defaultStack;
	}
	
	public boolean isEnchantable(ItemStack stack) { return getMaxCount() == 1; }
	
	public int getEnchantability() { return 5; }
	
	protected EntityWalker makeEntity(ServerWorld serverWorld, ItemStack stack, Consumer<EntityWalker> consumer, BlockPos pos)
	{
		EntityWalker walker = entityType.get().create(serverWorld, stack.getNbt(), consumer, pos, SpawnReason.SPAWN_EGG, true, true);
		if(walker != null)
			walker.copyFromItem(stack);
		return walker;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!stack.hasNbt())
			return;
		
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_left", getWheel(stack, Arm.LEFT).getName()));
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_right", getWheel(stack, Arm.RIGHT).getName()));
	}
	
	public static Iterable<ItemStack> getWheels(ItemStack stack)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, new ItemStack(WHCItems.WHEEL_OAK.get()));
		wheels.set(0, getWheel(stack, Arm.LEFT));
		wheels.set(1, getWheel(stack, Arm.RIGHT));
		return wheels;
	}
	
	public static void setWheels(ItemStack stack, ItemStack left, ItemStack right)
	{
		NbtCompound data = stack.getOrCreateNbt();
		
		NbtCompound wheels = new NbtCompound();
		wheels.put("Left", left.writeNbt(new NbtCompound()));
		wheels.put("Right", right.writeNbt(new NbtCompound()));
		data.put("Wheels", wheels);
		
		stack.setNbt(data);
	}
	
	public static void setHasChest(ItemStack stack, boolean contents)
	{
		NbtCompound data = stack.getOrCreateNbt();
		data.putBoolean("HasChest", contents);
		stack.setNbt(data);
	}
	
	public static boolean hasChest(ItemStack stack)
	{
		return stack.getOrCreateNbt().getBoolean("HasChest");
	}
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		String entry = arm == Arm.LEFT ? "Left" : "Right";
		ItemStack wheel = new ItemStack(WHCItems.WHEEL_OAK.get());
		if(stack.getItem() instanceof ItemWalker && stack.hasNbt())
		{
			NbtCompound data = stack.getNbt();
			if(data.contains("Wheels", NbtElement.COMPOUND_TYPE))
			{
				data = data.getCompound("Wheels");
				if(data.contains(entry, NbtElement.COMPOUND_TYPE))
					wheel = ItemStack.fromNbt(data.getCompound(entry));
			}
		}
		return wheel;
	}
}
