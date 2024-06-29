package com.lying.wheelchairs.utility;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.entity.EntityWalker;
import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

public class ServerEvents
{
	public static final Identifier EVENT_FIRST = new Identifier(Reference.ModInfo.MOD_ID, "first");
	public static final Identifier EVENT_LAST = new Identifier(Reference.ModInfo.MOD_ID, "last");
	
	/**
	 * Fired AFTER the player changes gamemode
	 */
	public static final Event<PlayerChangeGameMode> AFTER_PLAYER_CHANGE_GAME_MODE = EventFactory.createWithPhases(PlayerChangeGameMode.class, callbacks -> (player, mode) -> 
	{
		for(PlayerChangeGameMode callback : callbacks)
			callback.afterChangeGameMode(player, mode);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface PlayerChangeGameMode
	{
		void afterChangeGameMode(PlayerEntity player, GameMode gameMode);
	}
	
	/**
	 * Fired AFTER the player confirms a teleport
	 */
	public static final Event<PlayerConfirmTeleport> AFTER_PLAYER_TELEPORT = EventFactory.createWithPhases(PlayerConfirmTeleport.class, callbacks -> (player) -> 
	{
		for(PlayerConfirmTeleport callback : callbacks)
			callback.afterTeleport(player);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface PlayerConfirmTeleport
	{
		void afterTeleport(PlayerEntity player);
	}
	
	/**
	 * Fired AFTER a living entity changes its mount/vehicle
	 */
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT = EventFactory.createWithPhases(LivingChangeMount.class, callbacks -> (living, nextMount, lastMount) -> 
	{
		for(LivingChangeMount callback : callbacks)
			callback.afterChangeMount(living, nextMount, lastMount);
	}, EVENT_FIRST, Event.DEFAULT_PHASE, EVENT_LAST);
	
	@FunctionalInterface
	public interface LivingChangeMount
	{
		void afterChangeMount(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount);
	}
	
	public static final Event<DoubleJumpEvent> ON_DOUBLE_JUMP = EventFactory.createArrayBacked(DoubleJumpEvent.class, callbacks -> (living) -> 
	{
		for(DoubleJumpEvent callback : callbacks)
			callback.onDoubleJump(living);
	});
	
	/**
	 * Fired when a living entity jumps in midair
	 */
	@FunctionalInterface
	public interface DoubleJumpEvent
	{
		void onDoubleJump(LivingEntity living);
	}
	
	public static final Event<WalkerBindEvent> ON_WALKER_BIND = EventFactory.createArrayBacked(WalkerBindEvent.class, callbacks -> (living, walker) -> 
	{
		for(WalkerBindEvent callback : callbacks)
			callback.onBindToWalker(living, walker);
	});
	
	@FunctionalInterface
	public interface WalkerBindEvent
	{
		void onBindToWalker(LivingEntity living, EntityWalker walker);
	}
	
	public static final Event<StartFlyingEvent> ON_START_FLYING = EventFactory.createArrayBacked(StartFlyingEvent.class, callbacks -> (living) -> 
	{
		for(StartFlyingEvent callback : callbacks)
			callback.onStartFlying(living);
	});
	
	@FunctionalInterface
	public interface StartFlyingEvent
	{
		void onStartFlying(LivingEntity living);
	}
	
	public static final Event<StopFlyingEvent> ON_STOP_FLYING = EventFactory.createArrayBacked(StopFlyingEvent.class, callbacks -> (living) -> 
	{
		for(StopFlyingEvent callback : callbacks)
			callback.onStopFlying(living);
	});
	
	@FunctionalInterface
	public interface StopFlyingEvent
	{
		void onStopFlying(LivingEntity living);
	}
}
