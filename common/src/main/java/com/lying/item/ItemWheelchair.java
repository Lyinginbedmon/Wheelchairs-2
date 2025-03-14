package com.lying.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.entity.ChairUpgrade;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCDataComponentTypes;
import com.lying.init.WHCEntityTypes;
import com.lying.init.WHCItems;
import com.lying.init.WHCUpgrades;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWheelchair extends EntityPlacerItem<EntityWheelchair> implements IBonusBlockItem
{
	public ItemWheelchair(Settings settings)
	{
		super(WHCEntityTypes.WHEELCHAIR.get(), settings
				.component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(-6265536, true))
				.component(WHCDataComponentTypes.LEFT_WHEEL.get(), new ItemStack(WHCItems.WHEEL_OAK.get()))
				.component(WHCDataComponentTypes.RIGHT_WHEEL.get(), new ItemStack(WHCItems.WHEEL_OAK.get()))
				.component(WHCDataComponentTypes.UPGRADES.get(), Lists.newArrayList())
				.component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT));
	}
	
	public static ItemStack withWheels(Item chair, Item wheels)
	{
		ItemStack defaultStack = chair.getDefaultStack();
		setWheels(defaultStack, wheels.getDefaultStack(), wheels.getDefaultStack());
		return defaultStack;
	}
	
	public boolean isEnchantable(ItemStack stack) { return getMaxCount() == 1; }
	
	public int getEnchantability() { return 5; }
	
	protected EntityWheelchair makeEntity(ServerWorld serverWorld, ItemStack stack, @Nullable PlayerEntity player, BlockPos pos)
	{
		EntityWheelchair wheelchair = WHCEntityTypes.WHEELCHAIR.get().spawnFromItemStack(serverWorld, stack, player, pos, SpawnReason.SPAWN_ITEM_USE, true, true);
		if(wheelchair != null)
			wheelchair.copyFromItem(stack);
		return wheelchair;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_left", getWheel(stack, Arm.LEFT).getName()));
		tooltip.add(Text.translatable("gui.wheelchairs.wheelchair.wheel_right", getWheel(stack, Arm.RIGHT).getName()));
		
		List<Identifier> upgrades = stack.get(WHCDataComponentTypes.UPGRADES.get());
		if(upgrades.size() > 0)
		{
			tooltip.add(Text.translatable("gui.wheelchairs.upgrades"));
			for(Identifier id : upgrades)
			{
				ChairUpgrade upgrade = WHCUpgrades.get(id);
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
		stack.set(WHCDataComponentTypes.LEFT_WHEEL.get(), left.copy());
		stack.set(WHCDataComponentTypes.RIGHT_WHEEL.get(), right.copy());
	}
	
	public static ItemStack getWheel(ItemStack stack, Arm arm)
	{
		ComponentType<ItemStack> entry = arm == Arm.LEFT ? WHCDataComponentTypes.LEFT_WHEEL.get() : WHCDataComponentTypes.RIGHT_WHEEL.get();
		if(stack.contains(entry))
			return stack.get(entry);
		return new ItemStack(WHCItems.WHEEL_OAK);
	}
	
	public static boolean hasUpgrade(ItemStack stack, ChairUpgrade upgrade)
	{
		return stack.get(WHCDataComponentTypes.UPGRADES.get()).stream().anyMatch(up -> up.equals(upgrade.registryName()));
	}
}
