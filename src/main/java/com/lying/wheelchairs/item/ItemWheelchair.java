package com.lying.wheelchairs.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.entity.ChairUpgrade;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.init.WHCEntityTypes;
import com.lying.wheelchairs.init.WHCItems;
import com.lying.wheelchairs.init.WHCUpgrades;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWheelchair extends EntityPlacerItem<EntityWheelchair> implements DyeableItem, IBonusBlockItem
{
	public ItemWheelchair(Settings settings)
	{
		super(WHCEntityTypes.WHEELCHAIR, settings);
	}
	
	public static ItemStack withWheels(Item chair, Item wheels)
	{
		ItemStack defaultStack = chair.getDefaultStack();
		setWheels(defaultStack, wheels.getDefaultStack(), wheels.getDefaultStack());
		return defaultStack;
	}
	
	public boolean isEnchantable(ItemStack stack) { return getMaxCount() == 1; }
	
	public int getEnchantability() { return 5; }
	
	protected EntityWheelchair makeEntity(ServerWorld serverWorld, ItemStack stack, Consumer<EntityWheelchair> consumer, BlockPos pos)
	{
		EntityWheelchair wheelchair = WHCEntityTypes.WHEELCHAIR.create(serverWorld, stack.getNbt(), consumer, pos, SpawnReason.SPAWN_EGG, true, true);
		if(wheelchair != null)
			wheelchair.copyFromItem(stack);
		return wheelchair;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!stack.hasNbt())
			return;
		
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_left", getWheel(stack, Arm.LEFT).getName()));
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_right", getWheel(stack, Arm.RIGHT).getName()));
		
		NbtList upgrades = stack.getNbt().getList("Upgrades", NbtElement.STRING_TYPE);
		if(upgrades.size() > 0)
		{
			tooltip.add(Text.translatable("gui.wheelchairs.upgrades"));
			for(int i = 0; i<upgrades.size(); i++)
			{
				ChairUpgrade upgrade = WHCUpgrades.get(new Identifier(upgrades.getString(i)));
				if(upgrade != null)
					tooltip.add(Text.literal(" * ").append(upgrade.translate()));
			}
		}
	}
	
	public static Iterable<ItemStack> getWheels(ItemStack stack)
	{
		DefaultedList<ItemStack> wheels = DefaultedList.ofSize(2, new ItemStack(WHCItems.WHEEL_OAK));
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
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		String entry = arm == Arm.LEFT ? "Left" : "Right";
		ItemStack wheel = new ItemStack(WHCItems.WHEEL_OAK);
		if(stack.getItem() instanceof ItemWheelchair && stack.hasNbt())
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
	
	public static boolean hasUpgrade(ItemStack stack, ChairUpgrade upgrade)
	{
		NbtCompound data = stack.getOrCreateNbt();
		if(data.contains("Upgrades", NbtElement.LIST_TYPE))
		{
			NbtList list = data.getList("Upgrades", NbtElement.STRING_TYPE);
			for(int i=0; i<list.size(); i++)
				if(list.getString(i).equals(upgrade.registryName().toString()))
					return true;
		}
		return false;
	}
}
