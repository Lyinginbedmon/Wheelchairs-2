package com.lying.wheelchairs.entity;

import java.util.function.Consumer;

import org.apache.commons.lang3.function.Consumers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChairUpgrade
{
	private final Identifier name;
	private Predicate<ItemStack> isKeyItem = Predicates.alwaysFalse();
	
	private Consumer<EntityWheelchair> onApplied = Consumers.nop(), onRemoved = Consumers.nop();
	
	public ChairUpgrade(Identifier nameIn)
	{
		this.name = nameIn;
	}
	
	public final Identifier registryName() { return name; }
	
	public Text translate() { return Text.translatable("upgrade."+name.getNamespace()+"."+name.getPath()); }
	
	public final ChairUpgrade keyItem(Item itemIn) { this.isKeyItem = (stack) -> stack.getItem() == itemIn; return this; }
	
	public final ChairUpgrade keyItem(Predicate<ItemStack> itemIn) { this.isKeyItem = itemIn; return this; }
	
	public final ChairUpgrade applied(Consumer<EntityWheelchair> func) { this.onApplied = func; return this; }
	
	public final ChairUpgrade removed(Consumer<EntityWheelchair> func) { this.onRemoved = func; return this; }
	
	public boolean matches(ItemStack stack) { return isKeyItem.apply(stack); }
	
	public void applyTo(EntityWheelchair chair) { onApplied.accept(chair); }
	
	public void removeFrom(EntityWheelchair chair) { onRemoved.accept(chair); }
}
