package com.lying.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.utility.ChairspaceCondition;
import com.lying.utility.ServerEvents;

import dev.architectury.event.Event;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.util.Identifier;

/**
 * Chairspace conditions define the circumstances under which an entity was stored in Chairspace.<br>
 * They then also define the conditions under which the entity can be respawned from Chairspace.<br>
 * @author Lying
 *
 */
public class WHCChairspaceConditions
{
	private static final Map<Identifier, Supplier<ChairspaceCondition>> CONDITIONS = new HashMap<>();
	
	/** Respawn whenever the owner respawns */
	public static final Supplier<ChairspaceCondition> ON_RESPAWN = register(ChairspaceCondition.Builder.of("on_respawn", PlayerEvent.PLAYER_RESPAWN));
	
	/** Respawn whenever the owner logs in */
	public static final Supplier<ChairspaceCondition> ON_LOGIN = register(ChairspaceCondition.Builder.of("on_login", PlayerEvent.PLAYER_JOIN));
	
	/** Respawn when the server receives a teleport confirmation packet from the owner */
	public static final Supplier<ChairspaceCondition> ON_FINISH_TELEPORT = register(ChairspaceCondition.Builder.of("on_finish_teleport", ServerEvents.AFTER_PLAYER_TELEPORT));
	
	/** Respawn when the owner exits Spectator mode */
	public static final Supplier<ChairspaceCondition> ON_LEAVE_SPECTATOR = register(ChairspaceCondition.Builder.of("on_leave_spectator", ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE)
			.condition(player -> !player.isSpectator()));
	
	/** Respawn when the user stops fall-flying */
	public static final Supplier<ChairspaceCondition> ON_STOP_FLYING = register(ChairspaceCondition.Builder.of("on_stop_flying", ServerEvents.ON_STOP_FLYING));
	
	private static Supplier<ChairspaceCondition> register(ChairspaceCondition.Builder builder)
	{
		CONDITIONS.put(builder.registryName(), () -> builder.build());
		return CONDITIONS.get(builder.registryName());
	}
	
	public static void init() { }
	
	@Nullable
	public static ChairspaceCondition get(Identifier nameIn) { return CONDITIONS.getOrDefault(nameIn, () -> null).get(); }
	
	/** Returns a collection of all registered conditions listening to the given event */
	public static Collection<ChairspaceCondition> getApplicable(Event<?> eventIn)
	{
		Collection<ChairspaceCondition> conditions = Lists.newArrayList();
		CONDITIONS.values().stream().forEach(condition -> { if(condition.get().isListeningTo(eventIn)) conditions.add(condition.get()); });
		return conditions;
	}
}
