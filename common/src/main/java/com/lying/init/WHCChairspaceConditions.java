package com.lying.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.Wheelchairs;
import com.lying.chairspace.ChairspaceCondition;
import com.lying.reference.Reference;
import com.lying.utility.ServerEvents;

import dev.architectury.event.Event;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.LivingEntity;
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
	public static final Supplier<ChairspaceCondition> ON_RESPAWN = register("on_respawn", id -> ChairspaceCondition.Builder.of(id, PlayerEvent.PLAYER_RESPAWN));
	
	/** Respawn whenever the owner logs in */
	public static final Supplier<ChairspaceCondition> ON_LOGIN = register("on_login", id -> ChairspaceCondition.Builder.of(id, PlayerEvent.PLAYER_JOIN));
	
	/** Respawn when the server receives a teleport confirmation packet from the owner */	// FIXME Ensure teleporting works as intended
	public static final Supplier<ChairspaceCondition> ON_FINISH_TELEPORT = register("on_finish_teleport", id -> ChairspaceCondition.Builder.of(id, ServerEvents.AFTER_PLAYER_TELEPORT));
	
	/** Respawn when the owner exits Spectator mode */
	public static final Supplier<ChairspaceCondition> ON_LEAVE_SPECTATOR = register("on_leave_spectator", id -> ChairspaceCondition.Builder.of(id, ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE)
			.condition(player -> !player.isSpectator()));
	
	/** Respawn when the user stops fall-flying */
	public static final Supplier<ChairspaceCondition> ON_STOP_FLYING = register("on_stop_flying", id -> ChairspaceCondition.Builder.of(id, ServerEvents.ON_STOP_FLYING));
	
	public static final Supplier<ChairspaceCondition> ON_WAKE_UP = register("on_wake_up", id -> ChairspaceCondition.Builder.of(id, ServerEvents.ON_WAKE_UP)
			.postEffect(ent -> ((LivingEntity)ent).setHealth(1F)));
	
	private static Supplier<ChairspaceCondition> register(String nameIn, Function<String, ChairspaceCondition.Builder> supplier)
	{
		Supplier<ChairspaceCondition> finalised = () -> supplier.apply(nameIn).build();
		CONDITIONS.put(Reference.ModInfo.prefix(nameIn), finalised);
		return finalised;
	}
	
	public static void init()
	{
		Wheelchairs.LOGGER.info(" # Registered {} chairspace conditions", CONDITIONS.size());
	}
	
	@Nullable
	public static ChairspaceCondition get(Identifier nameIn) { return CONDITIONS.getOrDefault(nameIn, () -> null).get(); }
	
	/** Returns a collection of all registered conditions listening to the given event */
	public static Collection<ChairspaceCondition> getApplicable(Event<?> eventIn)
	{
		return CONDITIONS.values().stream().map(s -> s.get()).filter(c -> c.isListeningTo(eventIn)).toList();
	}
}
