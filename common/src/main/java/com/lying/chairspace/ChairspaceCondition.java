package com.lying.chairspace;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.init.WHCChairspaceConditions;
import com.lying.reference.Reference;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import dev.architectury.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * Defines the context of an entity being respawned.
 * @author Lying
 */
public class ChairspaceCondition
{
	public static final Codec<ChairspaceCondition> CODEC = Codec.of(ChairspaceCondition::encodeToOps, ChairspaceCondition::decodeFromOps);
	
	private static <T> DataResult<T> encodeToOps(final ChairspaceCondition func, final DynamicOps<T> ops, final T prefix)
	{
		return DataResult.success(ops.createString(func.registryName.toString()));
	}
	
	private static <T> DataResult<Pair<ChairspaceCondition, T>> decodeFromOps(final DynamicOps<T> ops, final T input)
	{
		ChairspaceCondition condition = WHCChairspaceConditions.get(Identifier.of(ops.getStringValue(input).getOrThrow()));
		return condition == null ? DataResult.error(() -> "Error reading Chairspace condition from data") : DataResult.success(Pair.of(condition, input));
	}
	
	private final Identifier registryName;
	private final List<Event<?>> firedBy = Lists.newArrayList();
	
	private final Predicate<Entity> canApplyTo;
	private final Consumer<Entity> postEffect;
	
	private ChairspaceCondition(Identifier nameIn, Predicate<Entity> qualifierIn, Consumer<Entity> postIn, Event<?>... events)
	{
		registryName = nameIn;
		canApplyTo = qualifierIn;
		postEffect = postIn;
		for(Event<?> event : events)
			firedBy.add(event);
	}
	
	public <T> T encode(DynamicOps<T> ops)
	{
		return CODEC.encodeStart(ops, this).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public static <T> ChairspaceCondition decode(DynamicOps<T> ops, T input)
	{
		return CODEC.parse(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
	}
	
	public Identifier registryName() { return this.registryName; }
	
	public boolean equals(Object obj) { return obj instanceof ChairspaceCondition && ((ChairspaceCondition)obj).registryName.equals(registryName); }
	
	public boolean isListeningTo(Event<?> event) { return firedBy.contains(event); }
	
	/** Additional caveats that must be met to exit chairspace under this condition */
	public boolean isApplicable(@NotNull Entity entity) { return this.canApplyTo.test(entity); }
	
	/** Applies any effects from exiting chairspace in this manner, such as visual effects */
	public void applyPostEffects(@Nullable Entity entity) { if(entity != null) this.postEffect.accept(entity); }
	
	public static class Builder
	{
		private final Identifier regName;
		private List<Event<?>> firedBy = Lists.newArrayList();
		private Predicate<Entity> canApplyTo = entity -> entity.isAlive();
		private Consumer<Entity> postEffect = Consumers.nop();
		
		private Builder(Identifier regNameIn)
		{
			regName = regNameIn;
		}
		
		public static Builder of(String nameIn) { return new Builder(Reference.ModInfo.prefix(nameIn)); }
		
		public static Builder of(String nameIn, Event<?> event)
		{
			return of(nameIn).listen(event);
		}
		
		public Builder condition(Predicate<Entity> conditionIn)
		{
			this.canApplyTo = conditionIn.and(canApplyTo);
			return this;
		}
		
		public Builder postEffect(Consumer<Entity> postIn)
		{
			this.postEffect = postIn;
			return this;
		}
		
		/**
		 * Sets this condition to be checked whenever the given event(s) are invoked.<br>
		 * Note that the event must still call {@link Chairspace.reactToEvent} to actually do the checking
		 */
		public Builder listen(Event<?>... eventIn)
		{
			for(Event<?> event : eventIn)
				firedBy.add(event);
			return this;
		}
		
		public ChairspaceCondition build() { return new ChairspaceCondition(regName, canApplyTo, postEffect, firedBy.toArray(new Event<?>[0])); }
	}
}