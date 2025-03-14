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
import net.minecraft.util.Identifier;

public class WHCUpgrades
{
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
			.incompatible(() -> List.of(WHCUpgrades.FLOATING, WHCUpgrades.POWERED)));
	public static final Supplier<ChairUpgrade> GLIDING = register(ChairUpgrade.Builder.of("gliding")
			.keyItem(Items.ELYTRA)
			.incompatible(() -> List.of(WHCUpgrades.POWERED)));
	public static final Supplier<ChairUpgrade> PLACER = register(ChairUpgrade.Builder.of("placer").modelled().enablesScreen()
			.keyItem(Items.DISPENSER));
	public static final Supplier<ChairUpgrade> HANDLES = register(ChairUpgrade.Builder.of("handles").modelled().enablesScreen()
			.keyItem(Items.IRON_BARS));
	
	private static Supplier<ChairUpgrade> register(ChairUpgrade.Builder builder)
	{
		ChairUpgrade made = builder.build();
		return UPGRADES.put(made.registryName(), () -> made);
	}
	
	public static void init()
	{
		UPGRADES.values().stream().map(s -> s.get()).filter(ChairUpgrade::hasModel).forEach(acc -> WHCBlocks.registerFakeBlock("upgrade_"+acc.registryName().getPath()));
		Wheelchairs.LOGGER.info(" # Registered {} wheelchair upgrades", UPGRADES.size());
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn) { return UPGRADES.getOrDefault(nameIn, () -> null).get(); }
	
	@Nullable
	public static Set<ChairUpgrade> fromItem(ItemStack stack, EntityWheelchair chair)
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
			ChairUpgrade upgrade = WHCUpgrades.get(list.get(i));
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
