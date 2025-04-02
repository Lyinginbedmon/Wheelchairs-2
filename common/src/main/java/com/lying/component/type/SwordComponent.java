package com.lying.component.type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

public record SwordComponent(ItemStack item) implements TooltipAppender
{
	public static final Codec<SwordComponent> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.optionalFieldOf("contents").forGetter(SwordComponent::contents))
				.apply(instance, (stack) -> new SwordComponent(stack.orElse(ItemStack.EMPTY))));
	public static final PacketCodec<RegistryByteBuf, SwordComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.xmap(SwordComponent::new, SwordComponent::item);
	
	public Optional<ItemStack> contents() { return item == null || item.isEmpty() ? Optional.empty() : Optional.of(item); }
	
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type)
	{
		contents().ifPresent(item -> tooltip.accept(Text.translatable("gui.wheelchairs.cane.sword", item.getName())));
	}
	
	public static SwordComponent empty() { return new SwordComponent(ItemStack.EMPTY); }
	
	public int hashCode() { return Objects.hash(item); }
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		else
			return obj instanceof SwordComponent comp && comp.item.equals(item);
	}
}
