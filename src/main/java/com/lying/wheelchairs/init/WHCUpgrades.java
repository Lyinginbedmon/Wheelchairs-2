package com.lying.wheelchairs.init;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.entity.ChairUpgrade;
import com.lying.wheelchairs.entity.EntityWheelchair;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class WHCUpgrades
{
	public static final RegistryKey<Registry<ChairUpgrade>> KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "chair_upgrade"));
	public static final Registry<ChairUpgrade> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();
	
	private static final List<ChairUpgrade> UPGRADES = Lists.newArrayList();
	
	public static final ChairUpgrade POWERED = register(ChairUpgrade.Builder.of("powered").modelled()
			.keyItem(Items.FURNACE_MINECART)
			.applied(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, true))
			.removed(chair -> chair.getDataTracker().set(EntityWheelchair.POWERED, false)));
	public static final ChairUpgrade STORAGE = register(ChairUpgrade.Builder.of("storage").modelled()
			.keyItem(stack -> (stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST)))
			.dropItem(Items.CHEST));
	public static final ChairUpgrade FLOATING = register(ChairUpgrade.Builder.of("floating").modelled()
			.keyItem(Items.PUMPKIN));
	public static final ChairUpgrade NETHERITE = register(ChairUpgrade.Builder.of("netherite").modelled()
			.keyItem(Items.NETHERITE_INGOT));
	public static final ChairUpgrade DIVING	= register(ChairUpgrade.Builder.of("diving").modelled()
			.keyItem(Items.LEATHER)
			.incompatible(() -> List.of(WHCUpgrades.FLOATING, WHCUpgrades.POWERED)));
	public static final ChairUpgrade GLIDING = register(ChairUpgrade.Builder.of("gliding")
			.keyItem(Items.ELYTRA)
			.incompatible(() -> List.of(WHCUpgrades.POWERED)));
	
	public static final ChairUpgrade HANDLES = register(ChairUpgrade.Builder.of("handles")	// TODO Reference walkers, incl. means for rider to unbind
			.keyItem(Items.IRON_BARS));
	public static final ChairUpgrade PLACER = register(ChairUpgrade.Builder.of("placer")	// TODO Auto-placer upgrade for bridge/pillar building
			.keyItem(Items.DISPENSER));
	
	private static ChairUpgrade register(ChairUpgrade.Builder builder)
	{
		ChairUpgrade made = builder.build();
		UPGRADES.add(made);
		return made;
	}
	
	public static void init()
	{
		UPGRADES.forEach(acc -> 
		{
			Registry.register(REGISTRY, acc.registryName(), acc);
			
			if(acc.hasModel())
				WHCBlocks.registerFakeBlock("upgrade_"+acc.registryName().getPath());
		});
	}
	
	@Nullable
	public static ChairUpgrade get(Identifier nameIn) { return REGISTRY.get(nameIn); }
	
	@Nullable
	public static Set<ChairUpgrade> fromItem(ItemStack stack, EntityWheelchair chair)
	{
		List<ChairUpgrade> existing = chair.getUpgrades();
		List<ChairUpgrade> upgrades = Lists.newArrayList();
		for(Identifier id : REGISTRY.getIds())
		{
			ChairUpgrade upgrade = get(id);
			if(upgrade.matches(stack) && existing.stream().allMatch(upg -> ChairUpgrade.canCombineWith(upg, upgrade)) && upgrade.canApplyTo(chair))
				upgrades.add(upgrade);
		}
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
	
	public static NbtList listToNbt(List<ChairUpgrade> upgrades)
	{
		NbtList list = new NbtList();
		upgrades.forEach(upg -> list.add(NbtString.of(upg.registryName().toString())));
		return list;
	}
}
