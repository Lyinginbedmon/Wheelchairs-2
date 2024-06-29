package com.lying.wheelchairs.init;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.utility.ChairspaceCondition;
import com.lying.wheelchairs.utility.ServerEvents;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Chairspace conditions define the circumstances under which an entity was stored in Chairspace.<br>
 * They then also define the conditions under which the entity can be respawned from Chairspace.<br>
 * @author Lying
 *
 */
public class WHCChairspaceConditions
{
	public static final RegistryKey<Registry<ChairspaceCondition>> KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "chairspace_condition"));
	public static final Registry<ChairspaceCondition> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();
	
	private static final List<ChairspaceCondition> CONDITIONS = Lists.newArrayList();
	
	/** Respawn whenever the owner respawns */
	public static final ChairspaceCondition ON_RESPAWN = register(ChairspaceCondition.Builder.of("on_respawn", ServerPlayerEvents.AFTER_RESPAWN));
	
	/** Respawn whenever the owner logs in */
	public static final ChairspaceCondition ON_LOGIN = register(ChairspaceCondition.Builder.of("on_login", ServerPlayConnectionEvents.JOIN));
	
	/** Respawn when the server receives a teleport confirmation packet from the owner */
	public static final ChairspaceCondition ON_FINISH_TELEPORT = register(ChairspaceCondition.Builder.of("on_finish_teleport", ServerEvents.AFTER_PLAYER_TELEPORT));
	
	/** Respawn when the owner exits Spectator mode */
	public static final ChairspaceCondition ON_LEAVE_SPECTATOR = register(ChairspaceCondition.Builder.of("on_leave_spectator", ServerEvents.AFTER_PLAYER_CHANGE_GAME_MODE)
			.condition(player -> !player.isSpectator()));
	
	/** Respawn when the user stops fall-flying */
	public static final ChairspaceCondition ON_STOP_FLYING = register(ChairspaceCondition.Builder.of("on_stop_flying", ServerEvents.ON_STOP_FLYING));
	
	private static ChairspaceCondition register(ChairspaceCondition.Builder builder)
	{
		ChairspaceCondition made = builder.build();
		CONDITIONS.add(made);
		return made;
	}
	
	public static void init()
	{
		CONDITIONS.forEach(acc -> Registry.register(REGISTRY, acc.registryName(), acc));
	}
	
	@Nullable
	public static ChairspaceCondition get(Identifier nameIn) { return REGISTRY.get(nameIn); }
	
	/** Returns a collection of all registered conditions listening to the given event */
	public static Collection<ChairspaceCondition> getApplicable(Event<?> eventIn)
	{
		Collection<ChairspaceCondition> conditions = Lists.newArrayList();
		REGISTRY.stream().forEach(condition -> { if(condition.isListeningTo(eventIn)) conditions.add(condition); });
		return conditions;
	}
}
