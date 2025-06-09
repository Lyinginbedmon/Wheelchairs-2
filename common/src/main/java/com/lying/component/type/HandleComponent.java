package com.lying.component.type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.lying.init.WHCItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

public record HandleComponent(ItemStack handle) implements TooltipAppender
{
	public static final Codec<HandleComponent> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.optionalFieldOf("contents").forGetter(HandleComponent::contents))
				.apply(instance, (stack) -> new HandleComponent(stack.orElse(ItemStack.EMPTY))));
	public static final PacketCodec<RegistryByteBuf, HandleComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.xmap(HandleComponent::new, HandleComponent::handle);
	
	public Optional<ItemStack> contents() { return handle == null || handle.isEmpty() ? Optional.of(WHCItems.HANDLE_OAK.get().getDefaultStack()) : Optional.of(handle); }
	
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type)
	{
		tooltip.accept(Text.translatable("gui.wheelchairs.cane.handle", contents().get().getName()));
	}
	
	public static HandleComponent empty() { return new HandleComponent(ItemStack.EMPTY); }
	
	public int hashCode() { return Objects.hash(handle); }
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		else
			return obj instanceof HandleComponent comp && comp.handle.equals(handle);
	}
}
