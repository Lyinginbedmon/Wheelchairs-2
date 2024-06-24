package com.lying.wheelchairs.entity;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface IParentedEntity
{
	public boolean hasParent();
	
	public boolean isParent(Entity entity);
	
	public void parentTo(@Nullable LivingEntity parent);
	
	public default void clearParent() { parentTo(null); }
	
	public default Vec3d getParentOffset(LivingEntity parent, float yaw, float pitch) { return Vec3d.ZERO; }
	
	public default void tickParented(@NotNull LivingEntity parent) { }
	
	@SuppressWarnings("unchecked")
	@Nullable
	public default <T extends LivingEntity & IParentedEntity> LivingEntity tryGetParent()
	{
		return this instanceof LivingEntity ? getParentOf((T)this) : null;
	}
	
	@Nullable
	public static <T extends LivingEntity & IParentedEntity> LivingEntity getParentOf(T entity)
	{
		Optional<LivingEntity> bestGuess = entity.getWorld().getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(16D), ent -> ent.isAlive() && entity.isParent(ent)).stream().findFirst();
		return bestGuess.orElseGet(() -> null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity & IParentedEntity> List<T> getParentedEntitiesOf(LivingEntity ent)
	{
		List<T> list = Lists.newArrayList();
		for(LivingEntity living : ent.getWorld().getEntitiesByClass(LivingEntity.class, ent.getBoundingBox().expand(16D), wal -> wal instanceof IParentedEntity && ((IParentedEntity)wal).isParent(ent)))
			list.add((T)living);
		return list;
	}
	
	public static boolean hasParentedEntities(LivingEntity ent)
	{
		return getParentedEntitiesOf(ent).isEmpty();
	}
	
	public static Vec3d getRotationVector(float pitch, float yaw)
	{
		float f = pitch * ((float)Math.PI / 180);
		float g = -yaw * ((float)Math.PI / 180);
		float h = MathHelper.cos((float)g);
		float i = MathHelper.sin((float)g);
		float j = MathHelper.cos((float)f);
		float k = MathHelper.sin((float)f);
		return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
	}
}
