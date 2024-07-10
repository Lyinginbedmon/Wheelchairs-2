package com.lying.wheelchairs.entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.Consumers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeModifierCreator;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChairUpgrade
{
	private final Identifier name;
	private final Predicate<ItemStack> isKeyItem;
	private final Item dropItem;
	private final boolean hasModel;
	private final boolean enablesScreen;
	
	private final Consumer<EntityWheelchair> onApplied, onRemoved;
	private final Map<EntityAttribute, AttributeModifierCreator> attributeModifiers = Maps.newHashMap();
	
	private final Predicate<EntityWheelchair> isValid;
	private final Supplier<List<ChairUpgrade>> incompatibleWith;
	
	protected ChairUpgrade(Identifier nameIn, boolean modelled, boolean screenEnabler,
			Predicate<ItemStack> keyItem, Item dropItem, Predicate<EntityWheelchair> valid, 
			Supplier<List<ChairUpgrade>> incompatibleWith, 
			Consumer<EntityWheelchair> applied, Consumer<EntityWheelchair> removed, Map<EntityAttribute, AttributeModifierCreator> modifiers)
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
	
	public Text translate() { return Text.translatable("upgrade."+name.getNamespace()+"."+name.getPath()); }
	
	public Item dropItem() { return dropItem; }
	
	public boolean matches(ItemStack stack) { return isKeyItem.apply(stack); }
	
	public boolean canApplyTo(EntityWheelchair chair) { return !chair.hasUpgrade(this) && isValid.apply(chair); }
	
	public final boolean compatibleWith(ChairUpgrade upgrade)
	{
		return this.incompatibleWith.get().isEmpty() ? true : this.incompatibleWith.get().stream().noneMatch(upg -> upg == upgrade);
	}
	
	/* Returns true if the given upgrades are mutually compatible */
	public static boolean canCombineWith(ChairUpgrade upgradeA, ChairUpgrade upgradeB) { return upgradeA.compatibleWith(upgradeB) && upgradeB.compatibleWith(upgradeA); }
	
	public void applyTo(EntityWheelchair chair) { onApplied.accept(chair); }
	
	public void removeFrom(EntityWheelchair chair) { onRemoved.accept(chair); }
	
	public void onStartRiding(LivingEntity rider)
	{
		if(!this.attributeModifiers.isEmpty())
			this.attributeModifiers.entrySet().forEach(entry -> 
			{
				EntityAttributeInstance instance = rider.getAttributes().getCustomInstance(entry.getKey());
				if(instance == null) return;
				
				instance.removeModifier(entry.getValue().getUuid());
				instance.addTemporaryModifier(entry.getValue().createAttributeModifier(0));
			});
	}
	
	public void onStopRiding(LivingEntity rider)
	{
		if(!this.attributeModifiers.isEmpty())
			this.attributeModifiers.entrySet().forEach(entry -> 
			{
				EntityAttributeInstance instance = rider.getAttributes().getCustomInstance(entry.getKey());
				if(instance != null)
					instance.removeModifier(entry.getValue().getUuid());
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
		private Predicate<EntityWheelchair> isValid = Predicates.alwaysTrue();
		private Supplier<List<ChairUpgrade>> incompatibleWith = () -> Lists.newArrayList();
		
		private boolean hasModel = false;
		
		private Consumer<EntityWheelchair> onApplied = Consumers.nop(), onRemoved = Consumers.nop();
		private final Map<EntityAttribute, AttributeModifierCreator> attributeModifiers = Maps.newHashMap();
		
		private boolean enablesScreen = false;
		
		protected Builder(Identifier nameIn) { this.name = nameIn; }
		
		public static Builder of(String nameIn) { return new Builder(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
		
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
		public final Builder isValid(Predicate<EntityWheelchair> validIn)
		{
			this.isValid = validIn;
			return this;
		}
		
		/** 
		 * Defines what upgrades this upgrade is not compatible with.<br>
		 * Upgrades must be mutually compatible to be applied to the same wheelchair
		 */
		public final Builder incompatible(Supplier<List<ChairUpgrade>> upgrades)
		{
			this.incompatibleWith = upgrades;
			return this;
		}
		
		/** Defines what this upgrade should do to a wheelchair when applied */
		public final Builder applied(Consumer<EntityWheelchair> func)
		{
			this.onApplied = func;
			return this;
		}
		
		/** Defines what this upgrade should do to a wheelchair when removed */
		public final Builder removed(Consumer<EntityWheelchair> func)
		{
			this.onRemoved = func;
			return this;
		}
		
		public final Builder attribute(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation)
		{
			this.attributeModifiers.put(attribute, new UpgradeAttributeModifierCreator(UUID.fromString(uuid), amount, operation));
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
		
		private class UpgradeAttributeModifierCreator implements AttributeModifierCreator
		{
			private final UUID uuid;
			private final double baseValue;
			private final EntityAttributeModifier.Operation operation;
			
			public UpgradeAttributeModifierCreator(UUID uuid, double baseValue, EntityAttributeModifier.Operation operation)
			{
				this.uuid = uuid;
				this.baseValue = baseValue;
				this.operation = operation;
			}
			
			public UUID getUuid() { return this.uuid; }
			
			public EntityAttributeModifier createAttributeModifier(int amplifier)
			{
				return new EntityAttributeModifier(this.uuid, ChairUpgrade.Builder.this.name.getPath(), this.baseValue, this.operation);
			}
		}
	}
}
