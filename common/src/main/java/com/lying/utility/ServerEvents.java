package com.lying.utility;

import org.jetbrains.annotations.Nullable;

import com.lying.reference.Reference;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
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
	public static final Event<PlayerChangeGameMode> AFTER_PLAYER_CHANGE_GAME_MODE = EventFactory.createLoop(PlayerChangeGameMode.class);
	
	@FunctionalInterface
	public interface PlayerChangeGameMode
	{
		void afterChangeGameMode(PlayerEntity player, GameMode gameMode);
	}
	
	/**
	 * Fired AFTER the player confirms a teleport
	 */
	public static final Event<PlayerConfirmTeleport> AFTER_PLAYER_TELEPORT = EventFactory.createLoop(PlayerConfirmTeleport.class);
	
	@FunctionalInterface
	public interface PlayerConfirmTeleport
	{
		void afterTeleport(PlayerEntity player);
	}
	
	/**
	 * Fired AFTER a living entity changes its mount/vehicle
	 */
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT_START = EventFactory.createLoop(LivingChangeMount.class);
	public static final Event<LivingChangeMount> AFTER_LIVING_CHANGE_MOUNT_END = EventFactory.createLoop(LivingChangeMount.class);
	
	@FunctionalInterface
	public interface LivingChangeMount
	{
		void afterChangeMount(LivingEntity living, @Nullable Entity nextMount, @Nullable Entity lastMount);
	}
	
	public static final Event<DoubleJumpEvent> ON_DOUBLE_JUMP = EventFactory.createLoop(DoubleJumpEvent.class);
	
	/**
	 * Fired when a living entity jumps in midair
	 */
	@FunctionalInterface
	public interface DoubleJumpEvent
	{
		void onDoubleJump(LivingEntity living);
	}
	
	public static final Event<WalkerBindEvent> ON_ENTITY_PARENT = EventFactory.createLoop(WalkerBindEvent.class);
	
	@FunctionalInterface
	public interface WalkerBindEvent
	{
		void onParentToEntity(LivingEntity living, LivingEntity walker);
	}
	
	public static final Event<StartFlyingEvent> ON_START_FLYING = EventFactory.createLoop(StartFlyingEvent.class);
	
	@FunctionalInterface
	public interface StartFlyingEvent
	{
		void onStartFlying(LivingEntity living);
	}
	
	public static final Event<StopFlyingEvent> ON_STOP_FLYING = EventFactory.createLoop(StopFlyingEvent.class);
	
	@FunctionalInterface
	public interface StopFlyingEvent
	{
		void onStopFlying(LivingEntity living);
	}
}
