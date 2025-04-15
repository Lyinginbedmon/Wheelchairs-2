package com.lying.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.Consumers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lying.Wheelchairs;
import com.lying.init.WHCChairUpgrades;
import com.lying.reference.Reference;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChairUpgrade
{
	public static final Codec<ChairUpgrade> CODEC	= Codec.of(ChairUpgrade::encodeToOps, ChairUpgrade::decodeFromOps);
	
	private static <T> DataResult<T> encodeToOps(final ChairUpgrade upg, final DynamicOps<T> ops, final T prefix)
	{
		return DataResult.success(ops.createString(upg.registryName().toString()));
	}
	
	private static <T> DataResult<Pair<ChairUpgrade, T>> decodeFromOps(final DynamicOps<T> ops, final T input)
	{
		ChairUpgrade condition = WHCChairUpgrades.get(Identifier.of(ops.getStringValue(input).getOrThrow()));
		return condition == null ? DataResult.error(() -> "Error reading wheelchair upgrade from data") : DataResult.success(Pair.of(condition, input));
	}
	
	public static final PacketCodec<RegistryByteBuf, ChairUpgrade> PACKET_CODEC	= PacketCodec.tuple(Identifier.PACKET_CODEC, ChairUpgrade::registryName, WHCChairUpgrades::get);
	
	private final Identifier name;
	private final Predicate<ItemStack> isKeyItem;
	private final Item dropItem;
	private final boolean hasModel;
	private final boolean enablesScreen;
	
	private final Consumer<WheelchairEntity> onApplied, onRemoved;
	private final Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifiers = Maps.newHashMap();
	
	private final Predicate<WheelchairEntity> isValid;
	private final Supplier<List<Supplier<ChairUpgrade>>> incompatibleWith;
	
	protected ChairUpgrade(Identifier nameIn, boolean modelled, boolean screenEnabler,
			Predicate<ItemStack> keyItem, Item dropItem, Predicate<WheelchairEntity> valid, 
			Supplier<List<Supplier<ChairUpgrade>>> incompatibleWith, 
			Consumer<WheelchairEntity> applied, Consumer<WheelchairEntity> removed, Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers)
	{
		this.name = nameIn;
		this.hasModel = modelled;
		this.enablesScreen = screenEnabler;
		this.isKeyItem = keyItem;
		this.dropItem = dropItem;
		this.isValid = valid;
		this.incompatibleWith = incompatibleWith;
		this.onApplied = applied;
		this.onRemoved = removed;
		modifiers.entrySet().forEach(entry -> 
		{
			this.attributeModifiers.put(entry.getKey(), entry.getValue());
		});
	}
	
	public final Identifier registryName() { return name; }
	
	public boolean equals(Object obj) { return obj instanceof ChairUpgrade && ((ChairUpgrade)obj).registryName().equals(name); }
	
	public <T> T encode(DynamicOps<T> ops)
	{
		return CODEC.encodeStart(ops, this).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public static <T> ChairUpgrade decode(DynamicOps<T> ops, T input)
	{
		return CODEC.parse(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public static <T> T encodeList(DynamicOps<T> ops, List<ChairUpgrade> list)
	{
		return CODEC.listOf().encodeStart(ops, list).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public static <T> List<ChairUpgrade> decodeList(DynamicOps<T> ops, T input)
	{
		return CODEC.listOf().parse(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public Text translate() { return Text.translatable("upgrade."+name.getNamespace()+"."+name.getPath()); }
	
	public Item dropItem() { return dropItem; }
	
	public boolean matches(ItemStack stack) { return isKeyItem.apply(stack); }
	
	public boolean canApplyTo(WheelchairEntity chair) { return !chair.hasUpgrade(this) && isValid.apply(chair); }
	
	public final boolean compatibleWith(ChairUpgrade upgrade)
	{
		return this.incompatibleWith.get().isEmpty() ? true : this.incompatibleWith.get().stream().noneMatch(upg -> upg == upgrade);
	}
	
	/* Returns true if the given upgrades are mutually compatible */
	public static boolean canCombineWith(ChairUpgrade upgradeA, ChairUpgrade upgradeB) { return upgradeA.compatibleWith(upgradeB) && upgradeB.compatibleWith(upgradeA); }
	
	public void applyTo(WheelchairEntity chair) { onApplied.accept(chair); }
	
	public void removeFrom(WheelchairEntity chair) { onRemoved.accept(chair); }
	
	public void onStartRiding(LivingEntity rider)
	{
		if(!this.attributeModifiers.isEmpty())
			this.attributeModifiers.entrySet().forEach(entry -> 
			{
				EntityAttributeInstance instance = rider.getAttributes().getCustomInstance(entry.getKey());
				if(instance == null || instance.hasModifier(entry.getValue().id())) return;
				
				instance.addTemporaryModifier(entry.getValue());
			});
	}
	
	public void onStopRiding(LivingEntity rider)
	{
		if(!this.attributeModifiers.isEmpty())
			this.attributeModifiers.entrySet().forEach(entry -> 
			{
				EntityAttributeInstance instance = rider.getAttributes().getCustomInstance(entry.getKey());
				if(instance != null)
					instance.removeModifier(entry.getValue());
			});
	}
	
	public boolean hasModel() { return hasModel; }
	
	public boolean enablesScreen() { return enablesScreen; }
	
	/** Builder class to restrict modifications to before registration */
	public static class Builder
	{
		private final Identifier name;
		private Predicate<ItemStack> isKeyItem = Predicates.alwaysFalse();
		private Item dropItem = Items.STICK;
		private Predicate<WheelchairEntity> isValid = Predicates.alwaysTrue();
		private Supplier<List<Supplier<ChairUpgrade>>> incompatibleWith = () -> Lists.newArrayList();
		
		private boolean hasModel = false;
		
		private Consumer<WheelchairEntity> onApplied = Consumers.nop(), onRemoved = Consumers.nop();
		private final Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifiers = new HashMap<>();
		
		private boolean enablesScreen = false;
		
		protected Builder(Identifier nameIn) { this.name = nameIn; }
		
		public static Builder of(String nameIn) { return new Builder(Reference.ModInfo.prefix(nameIn)); }
		
		/** Defines the item needed to apply this upgrade to a wheelchair */
		public final Builder keyItem(Item itemIn)
		{
			dropItem = itemIn;
			keyItem((stack) -> stack.getItem() == itemIn);
			return this;
		}
		
		/** Defines an {@link ItemStack} predicate to apply this upgrade to a wheelchair */
		public final Builder keyItem(Predicate<ItemStack> itemIn)
		{
			this.isKeyItem = itemIn;
			return this;
		}
		
		public final Builder dropItem(Item itemIn)
		{
			dropItem = itemIn;
			return this;
		}
		
		/** Defines the properties a wheelchair must have to apply this upgrade*/
		public final Builder isValid(Predicate<WheelchairEntity> validIn)
		{
			this.isValid = validIn;
			return this;
		}
		
		/** 
		 * Defines what upgrades this upgrade is not compatible with.<br>
		 * Upgrades must be mutually compatible to be applied to the same wheelchair
		 */
		public final Builder incompatible(Supplier<List<Supplier<ChairUpgrade>>> upgrades)
		{
			this.incompatibleWith = upgrades;
			return this;
		}
		
		/** Defines what this upgrade should do to a wheelchair when applied */
		public final Builder applied(Consumer<WheelchairEntity> func)
		{
			this.onApplied = func;
			return this;
		}
		
		/** Defines what this upgrade should do to a wheelchair when removed */
		public final Builder removed(Consumer<WheelchairEntity> func)
		{
			this.onRemoved = func;
			return this;
		}
		
		public final Builder attribute(RegistryEntry<EntityAttribute> attribute, String uuid, double amount, EntityAttributeModifier.Operation operation)
		{
			this.attributeModifiers.put(attribute, new EntityAttributeModifier(name, amount, operation));
			return this;
		}
		
		/** Registers that this upgrade has an associated block model to render */
		public final Builder modelled()
		{
			this.hasModel = true;
			return this;
		}
		
		public final Builder enablesScreen()
		{
			this.enablesScreen = true;
			return this;
		}
		
		public ChairUpgrade build()
		{
			return new ChairUpgrade(name, hasModel, enablesScreen, isKeyItem, dropItem, isValid, incompatibleWith, onApplied, onRemoved, attributeModifiers);
		}
	}
}
