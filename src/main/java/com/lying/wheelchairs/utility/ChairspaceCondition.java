package com.lying.wheelchairs.utility;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * Defines the context of an entity being respawned.
 * @author Lying
 */
public class ChairspaceCondition
{
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
	
	public Identifier registryName() { return this.registryName; }
	
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
		
		public static Builder of(String nameIn) { return new Builder(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
		
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