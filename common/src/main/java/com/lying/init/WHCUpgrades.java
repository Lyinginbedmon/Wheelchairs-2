package com.lying.init;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.entity.ChairUpgrade;
import com.lying.entity.EntityWheelchair;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class WHCUpgrades
{
	private static final Identifier registryId = new Identifier(Reference.ModInfo.MOD_ID, "chair_upgrade");
	public static final Registrar<ChairUpgrade> REGISTRY = RegistrarManager.get(Reference.ModInfo.MOD_ID).<ChairUpgrade>builder(registryId).build();
	public static final RegistryKey<? extends Registry<ChairUpgrade>> KEY = REGISTRY.key();
	
	private static final List<ChairUpgrade> UPGRADES = Lists.newArrayList();
	
	public static final RegistrySupplier<ChairUpgrade> POWERED = register(ChairUpgrade.Builder.of("powered").modelled()
			.keyItem(Items.FURNACE_MINECART)
			.applied(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, true))
			.removed(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, false)));
	public static final RegistrySupplier<ChairUpgrade> STORAGE = register(ChairUpgrade.Builder.of("storage").modelled()
			.keyItem(stack -> (stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST))));
	public static final RegistrySupplier<ChairUpgrade> FLOATING = register(ChairUpgrade.Builder.of("floating").modelled()
			.keyItem(Items.PUMPKIN));
	public static final RegistrySupplier<ChairUpgrade> NETHERITE = register(ChairUpgrade.Builder.of("netherite").modelled()
			.keyItem(Items.NETHERITE_INGOT));
	public static final RegistrySupplier<ChairUpgrade> DIVING	= register(ChairUpgrade.Builder.of("diving").modelled()
			.keyItem(Items.LEATHER)
			.incompatible(() -> List.of(WHCUpgrades.FLOATING.get(), WHCUpgrades.POWERED.get())));
	public static final RegistrySupplier<ChairUpgrade> GLIDING = register(ChairUpgrade.Builder.of("gliding")
			.keyItem(Items.ELYTRA)
			.incompatible(() -> List.of(WHCUpgrades.POWERED.get())));
	
	public static final RegistrySupplier<ChairUpgrade> HANDLES = register(ChairUpgrade.Builder.of("handles")	// TODO Reference zimmer frames, incl. means for rider to unbind
			.keyItem(Items.IRON_BARS));
	public static final RegistrySupplier<ChairUpgrade> PLACER = register(ChairUpgrade.Builder.of("placer")	// TODO Auto-placer upgrade for bridge/pillar building
			.keyItem(Items.DISPENSER));
	
	private static RegistrySupplier<ChairUpgrade> register(ChairUpgrade.Builder builder)
	{
		return REGISTRY.register(builder.registryName(), () -> builder.build());
	}
	
	public static void init()
	{
		UPGRADES.forEach(acc -> 
		{
			REGISTRY.register(acc.registryName(), () -> acc);
			
			if(acc.hasModel())
				WHCBlocks.registerFakeBlock("upgrade_"+acc.registryName().getPath());
		});
		Wheelchairs.LOGGER.info("Registered "+REGISTRY.entrySet().size()+" upgrades");
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn)
	{
		return REGISTRY.get(nameIn);
	}
	
	@Nullable
	public static Set<ChairUpgrade> fromItem(ItemStack stack, EntityWheelchair chair)
	{
		List<ChairUpgrade> existing = chair.getUpgrades();
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		REGISTRY.forEach(upgrade ->
		{
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
