package com.lying.component.type;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.lying.init.WHCItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;

public class WheelComponent implements TooltipAppender
{
	public static final Supplier<ItemStack> DEFAULT_WHEEL = () -> new ItemStack(WHCItems.WHEEL_OAK);
	
	public static final Codec<WheelComponent> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.optionalFieldOf("contents").forGetter(c -> Optional.of(c.item)),
			Arm.CODEC.fieldOf("side").forGetter(WheelComponent::side))
				.apply(instance, (stack, side) -> stack.isPresent() ? new WheelComponent(stack.get(), side) : new WheelComponent(side)));
	
	public static final PacketCodec<RegistryByteBuf, WheelComponent> PACKET_CODEC = PacketCodec.tuple(
			ItemStack.OPTIONAL_PACKET_CODEC, WheelComponent::item, 
			PacketCodecs.BOOLEAN, WheelComponent::isRight, 
			(stack, side) -> new WheelComponent(stack, side ? Arm.RIGHT : Arm.LEFT));
	
	private final Arm side;
	private ItemStack item = DEFAULT_WHEEL.get();
	
	protected WheelComponent(@NotNull ItemStack item, Arm side)
	{
		this(side);
		with(item);
	}
	
	protected WheelComponent(Arm side)
	{
		this.side = side;
	}
	
	public final Arm side() { return side; }
	
	public final boolean isRight() { return side == Arm.RIGHT; }
	
	public ItemStack item() { return item; }
	
	public WheelComponent with(ItemStack stack)
	{
		if(stack == null || stack.isEmpty())
			return this;
		
		this.item = stack.copy();
		return this;
	}
	
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type)
	{
		tooltip.accept(Text.translatable("gui.wheelchairs.wheelchair.wheel_"+side.name().toLowerCase(), item.getName()));
	}
	
	public static WheelComponent empty(Arm side) { return new WheelComponent(side); }
}
