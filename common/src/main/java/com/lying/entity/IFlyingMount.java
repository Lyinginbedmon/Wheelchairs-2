package com.lying.entity;

import net.minecraft.entity.Mount;

public interface IFlyingMount extends Mount
{
	/** Returns true if the mount meets the prequisites for flying */
	public boolean canFly();
	
	/** Returns true if the mount is currently flying, usually the same as LivingEntity.isFallFlying */
	public boolean isFlying();
	
	public default boolean canStartFlying() { return canFly() && !isFlying(); }
	
	/** Returns true if the mount is able to use firework rockets for boosting */
	public default boolean canUseRocket() { return false; }
	
	/**
	 * Returns true if the mount can use a firework rocket specifically now.<br>
	 * By default this is the combined value of {@link isFlying} and {@link canUseRocket};
	 */
	public default boolean canUseRocketNow() { return isFlying() && canUseRocket(); }
	
	public default void startFlying() { }
	
	public default void stopFlying() { }
}
