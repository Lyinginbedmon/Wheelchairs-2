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
import net.minecraft.util.Identifier;

public class WHCUpgrades
{
//	private static final Identifier REGISTRY_ID = new Identifier(Reference.ModInfo.MOD_ID, "chair_upgrade");
//	public static final Registrar<ChairUpgrade> REGISTRY = RegistrarManager.get(Reference.ModInfo.MOD_ID).<ChairUpgrade>builder(REGISTRY_ID).build();
//	public static final RegistryKey<? extends Registry<ChairUpgrade>> KEY = REGISTRY.key();
	
	// TODO Replace with custom object registry
	private static final Map<Identifier, Supplier<ChairUpgrade>> UPGRADES = new HashMap<>();
	
	public static final Supplier<ChairUpgrade> POWERED = register(ChairUpgrade.Builder.of("powered").modelled()
			.keyItem(Items.FURNACE_MINECART)
			.applied(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, true))
			.removed(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, false)));
	public static final Supplier<ChairUpgrade> STORAGE = register(ChairUpgrade.Builder.of("storage").modelled()
			.keyItem(stack -> (stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST))));
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
	
	public static final Supplier<ChairUpgrade> HANDLES = register(ChairUpgrade.Builder.of("handles")	// TODO Reference zimmer frames, incl. means for rider to unbind
			.keyItem(Items.IRON_BARS));
	public static final Supplier<ChairUpgrade> PLACER = register(ChairUpgrade.Builder.of("placer")	// TODO Auto-placer upgrade for bridge/pillar building
			.keyItem(Items.DISPENSER));
	
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
		Wheelchairs.LOGGER.info("Registered "+UPGRADES.size()+" upgrades");
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn)
	{
		return UPGRADES.get(nameIn).get();
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
		for(int i=0; i<list.size(); i++)
		{
			ChairUpgrade upgrade = WHCUpgrades.get(new Identifier(list.getString(i)));
			if(upgrade != null)
				upgrades.add(upgrade);
		}
		return upgrades;
	}
}
