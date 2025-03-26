package com.lying.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.entity.ChairUpgrade;
import com.lying.entity.WheelchairEntity;
import com.lying.reference.Reference;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class WHCChairUpgrades
{
	private static final Map<Identifier, Supplier<ChairUpgrade>> UPGRADES = new HashMap<>();
	
	public static final Supplier<ChairUpgrade> POWERED = register("powered", b -> b.modelled()
			.keyItem(Items.FURNACE_MINECART)
			.applied(chair -> chair.getDataTracker().set(WheelchairEntity.POWERED, true))
			.removed(chair -> chair.getDataTracker().set(WheelchairEntity.POWERED, false)));
	public static final Supplier<ChairUpgrade> STORAGE = register("storage", b -> b.modelled().enablesScreen()
			.keyItem(stack -> (stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST)))
			.dropItem(Items.CHEST));
	public static final Supplier<ChairUpgrade> FLOATING = register("floating", b -> b.modelled()
			.keyItem(Items.PUMPKIN));
	public static final Supplier<ChairUpgrade> NETHERITE = register("netherite", b -> b
			.modelled()
			.keyItem(Items.NETHERITE_INGOT));
	public static final Supplier<ChairUpgrade> DIVING	= register("diving", b -> b.modelled()
			.keyItem(Items.LEATHER)
			.incompatible(() -> List.of(WHCChairUpgrades.FLOATING, WHCChairUpgrades.POWERED)));
	public static final Supplier<ChairUpgrade> GLIDING = register("gliding", b -> b
			.keyItem(Items.ELYTRA)
			.incompatible(() -> List.of(WHCChairUpgrades.POWERED)));
	public static final Supplier<ChairUpgrade> PLACER = register("placer", b -> b.modelled().enablesScreen()
			.keyItem(Items.DISPENSER));
	public static final Supplier<ChairUpgrade> HANDLES = register("handles", b -> b.modelled().enablesScreen()
			.keyItem(Items.IRON_BARS));
	
	private static Supplier<ChairUpgrade> register(String nameIn, Consumer<ChairUpgrade.Builder> consumer)
	{
		ChairUpgrade.Builder builder = ChairUpgrade.Builder.of(nameIn);
		consumer.accept(builder);
		Supplier<ChairUpgrade> supplier = () -> builder.build();
		UPGRADES.put(Reference.ModInfo.prefix(nameIn), supplier);
		return supplier;
	}
	
	public static void init()
	{
		UPGRADES.values().stream().map(s -> s.get()).filter(ChairUpgrade::hasModel).forEach(acc -> WHCBlocks.registerFakeBlock("upgrade_"+acc.registryName().getPath()));
		Wheelchairs.LOGGER.info(" # Registered {} wheelchair upgrades", UPGRADES.size());
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn) { return UPGRADES.getOrDefault(nameIn, () -> null).get(); }
	
	@Nullable
	public static Set<ChairUpgrade> fromItem(ItemStack stack, WheelchairEntity chair)
	{
		List<ChairUpgrade> existing = chair.getUpgrades();
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		for(Identifier id : UPGRADES.keySet())
		{
			ChairUpgrade upgrade = get(id);
			if(upgrade.matches(stack) && existing.stream().allMatch(upg -> ChairUpgrade.canCombineWith(upg, upgrade)) && upgrade.canApplyTo(chair))
				upgrades.add(upgrade);
		}
		return Set.of(upgrades.toArray(new ChairUpgrade[0]));
	}
	
	public static List<ChairUpgrade> idsToList(List<Identifier> list)
	{
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		for(int i=0; i<list.size(); i++)
		{
			ChairUpgrade upgrade = WHCChairUpgrades.get(list.get(i));
			if(upgrade != null)
				upgrades.add(upgrade);
		}
		return upgrades;
	}
	
	public static List<Identifier> listToIds(List<ChairUpgrade> upgrades)
	{
		List<Identifier> list = Lists.newArrayList();
		upgrades.forEach(upg -> list.add(upg.registryName()));
		return list;
	}
}
