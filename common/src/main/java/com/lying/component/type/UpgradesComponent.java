package com.lying.component.type;

import java.util.List;
import java.util.function.Consumer;

import com.lying.entity.ChairUpgrade;
import com.lying.init.WHCChairUpgrades;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record UpgradesComponent(List<Identifier> upgrades) implements TooltipAppender
{
	public static final Codec<UpgradesComponent> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.listOf().fieldOf("contents").forGetter(UpgradesComponent::upgrades))
				.apply(instance, UpgradesComponent::new));
	public static final PacketCodec<ByteBuf, UpgradesComponent> PACKET_CODEC = Identifier.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(UpgradesComponent::new, UpgradesComponent::upgrades);
	
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type)
	{
		// TODO Finalise upgrade list formatting
		upgrades.forEach(upgrade -> 
		{
			ChairUpgrade obj = WHCChairUpgrades.get(upgrade);
			if(obj != null)
				tooltip.accept(obj.translate());
		});
	}
	
	public static UpgradesComponent blank() { return new UpgradesComponent(List.of()); }
}
