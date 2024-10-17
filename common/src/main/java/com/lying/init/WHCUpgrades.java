package com.lying.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.entity.ChairUpgrade;
import com.lying.entity.EntityWheelchair;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public class WHCUpgrades
{
	// TODO Replace with custom object registry
	private static final Map<Identifier, Supplier<ChairUpgrade>> UPGRADES = new HashMap<>();
	
	public static final Supplier<ChairUpgrade> POWERED = register(ChairUpgrade.Builder.of("powered").modelled()
			.keyItem(Items.FURNACE_MINECART)
			.applied(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, true))
			.removed(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, false)));
	public static final Supplier<ChairUpgrade> STORAGE = register(ChairUpgrade.Builder.of("storage").modelled().enablesScreen()
			.keyItem(stack -> (stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST)))
			.dropItem(Items.CHEST));
	public static final Supplier<ChairUpgrade> FLOATING = register(ChairUpgrade.Builder.of("floating").modelled()
			.keyItem(Items.PUMPKIN));
	public static final Supplier<ChairUpgrade> NETHERITE = register(ChairUpgrade.Builder.of("netherite").modelled()
			.keyItem(Items.NETHERITE_INGOT));
	public static final Supplier<ChairUpgrade> DIVING	= register(ChairUpgrade.Builder.of("diving").modelled()
			.keyItem(Items.LEATHER)
			.incompatible(() -> List.of(WHCUpgrades.FLOATING.get(), WHCUpgrades.POWERED.get())));
	public static final Supplier<ChairUpgrade> GLIDING = register(ChairUpgrade.Builder.of("gliding")
			.keyItem(Items.ELYTRA)
			.incompatible(() -> List.of(WHCUpgrades.POWERED.get())));
	public static final Supplier<ChairUpgrade> PLACER = register(ChairUpgrade.Builder.of("placer").modelled().enablesScreen()
			.keyItem(Items.DISPENSER));
	public static final Supplier<ChairUpgrade> HANDLES = register(ChairUpgrade.Builder.of("handles").modelled().enablesScreen()
			.keyItem(Items.IRON_BARS));
	
	private static Supplier<ChairUpgrade> register(ChairUpgrade.Builder builder)
	{
		UPGRADES.put(builder.registryName(), () -> builder.build());
		return UPGRADES.get(builder.registryName());
	}
	
	public static void init()
	{
		UPGRADES.values().forEach(entry -> 
		{
			ChairUpgrade acc = entry.get();
			if(acc.hasModel())
				WHCBlocks.registerFakeBlock("upgrade_"+acc.registryName().getPath());
		});
		Wheelchairs.LOGGER.info(" # Registered "+UPGRADES.size()+" wheelchair upgrades");
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn)
	{
		return UPGRADES.getOrDefault(nameIn, () -> null).get();
	}
	
	@Nullable
	public static Set<ChairUpgrade> fromItem(ItemStack stack, EntityWheelchair chair)
	{
		List<ChairUpgrade> existing = chair.getUpgrades();
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		UPGRADES.values().forEach(entry ->
		{
			ChairUpgrade upgrade = entry.get();
			if(upgrade.matches(stack) && existing.stream().allMatch(upg -> ChairUpgrade.canCombineWith(upg, upgrade)) && upgrade.canApplyTo(chair))
				upgrades.add(upgrade);
		});
		return Set.of(upgrades.toArray(new ChairUpgrade[0]));
	}
	
	public static List<ChairUpgrade> nbtToList(NbtList list)
	{
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		list.forEach(element -> 
		{
			ChairUpgrade upgrade = WHCUpgrades.get(new Identifier(element.asString()));
			if(upgrade != null)
				upgrades.add(upgrade);
		});
		return upgrades;
	}
	
	public static NbtList listToNbt(List<ChairUpgrade> upgrades)
	{
		NbtList list = new NbtList();
		upgrades.forEach(upg -> list.add(NbtString.of(upg.registryName().toString())));
		return list;
	}
}
