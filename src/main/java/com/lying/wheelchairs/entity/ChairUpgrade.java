package com.lying.wheelchairs.entity;

import java.util.function.Consumer;

import org.apache.commons.lang3.function.Consumers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChairUpgrade
{
	private final Identifier name;
	private final Predicate<ItemStack> isKeyItem;
	private final boolean hasModel;
	private final Consumer<EntityWheelchair> onApplied, onRemoved;
	
	protected ChairUpgrade(Identifier nameIn, boolean modelled, Predicate<ItemStack> keyItem, Consumer<EntityWheelchair> applied, Consumer<EntityWheelchair> removed)
	{
		this.name = nameIn;
		this.hasModel = modelled;
		this.isKeyItem = keyItem;
		this.onApplied = applied;
		this.onRemoved = removed;
	}
	
	public final Identifier registryName() { return name; }
	
	public Text translate() { return Text.translatable("upgrade."+name.getNamespace()+"."+name.getPath()); }
	
	public boolean matches(ItemStack stack) { return isKeyItem.apply(stack); }
	
	public void applyTo(EntityWheelchair chair) { onApplied.accept(chair); }
	
	public void removeFrom(EntityWheelchair chair) { onRemoved.accept(chair); }
	
	public boolean hasModel() { return hasModel; }
	
	/** Builder class to restrict modifications to before registration */
	public static class Builder
	{
		private final Identifier name;
		private Predicate<ItemStack> isKeyItem = Predicates.alwaysFalse();
		
		private boolean hasModel = false;
		
		private Consumer<EntityWheelchair> onApplied = Consumers.nop(), onRemoved = Consumers.nop();
		
		protected Builder(Identifier nameIn) { this.name = nameIn; }
		
		public static Builder of(String nameIn) { return new Builder(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
		
		/** Defines the item needed to apply this upgrade to a wheelchair */
		public final Builder keyItem(Item itemIn) { keyItem((stack) -> stack.getItem() == itemIn); return this; }
		
		/** Defines an {@link ItemStack} predicate to apply this upgrade to a wheelchair */
		public final Builder keyItem(Predicate<ItemStack> itemIn)
		{
			this.isKeyItem = itemIn;
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
		
		/** Registers that this upgrade has an associated block model to render */
		public final Builder modelled()
		{
			this.hasModel = true;
			return this;
		}
		
		public ChairUpgrade build()
		{
			return new ChairUpgrade(name, hasModel, isKeyItem, onApplied, onRemoved);
		}
	}
}
