package com.lying.wheelchairs.entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.init.WHCSoundEvents;
import com.lying.wheelchairs.utility.ServerEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface IParentedEntity
{
	public static final double SEARCH_RANGE = 8D;
	
	public static Predicate<Entity> isChildOf(LivingEntity entity) { return ent -> ent.isAlive() && ent instanceof IParentedEntity && ((IParentedEntity)ent).isParent(entity); } 
	
	public boolean hasParent();
	
	public boolean isParent(Entity entity);
	
	@Nullable
	public LivingEntity getParent();
	
	public void parentTo(@Nullable LivingEntity parent);
	
	public default void clearParent() { parentTo(null); }
	
	public default Vec3d getParentOffset(LivingEntity parent, float yaw, float pitch) { return Vec3d.ZERO; }
	
	public default void tickParented(@NotNull LivingEntity parent, float yaw, float pitch) { }
	
	@SuppressWarnings("unchecked")
	@Nullable
	public default <T extends LivingEntity & IParentedEntity> LivingEntity tryGetParent()
	{
		return this instanceof LivingEntity ? getParentOf((T)this) : null;
	}
	
	@Nullable
	public static <T extends LivingEntity & IParentedEntity> LivingEntity getParentOf(T entity)
	{
		Optional<LivingEntity> bestGuess = entity.getWorld().getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(SEARCH_RANGE), ent -> ent.isAlive() && entity.isParent(ent)).stream().findFirst();
		return bestGuess.orElseGet(() -> null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity & IParentedEntity> List<T> getParentedEntitiesOf(LivingEntity ent)
	{
		List<T> list = Lists.newArrayList();
		for(LivingEntity living : ent.getWorld().getEntitiesByClass(LivingEntity.class, ent.getBoundingBox().expand(SEARCH_RANGE), wal -> wal instanceof IParentedEntity && ((IParentedEntity)wal).isParent(ent)))
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
	
	public default <T extends LivingEntity & IParentedEntity> boolean canParentToChild(LivingEntity parent, T child)
	{
		return !parent.hasVehicle() && parent.distanceTo(child) < 5D && (!Wheelchairs.config.handsyWalkers() || (child.getMainHandStack().isEmpty() || child.getOffHandStack().isEmpty()));
	}
	
	/** Updates the position and rotation of the child entity according to the position and rotation of the parent entity */
	public static void updateParentingBond(LivingEntity child, LivingEntity parent)
	{
		if(child == null || parent == null || parent == child || !(child instanceof IParentedEntity) || !((IParentedEntity)child).isParent(parent))
			return;
		
		float yaw = parent.getBodyYaw();
		float pitch = parent.getPitch();
		
		child.setVelocity(Vec3d.ZERO);
		child.tick();
		
		IParentedEntity parented = (IParentedEntity)child;
		parented.tickParented(parent, yaw, pitch);
		
		// Stop calculating if the parented tick has severed the parenting bond
		if(!parented.hasParent())
			return;
		
		child.setPosition(parent.getPos().add(parented.getParentOffset(parent, yaw, pitch)));
	}
	
	public static <T extends LivingEntity & IParentedEntity> boolean bindToPlayer(PlayerEntity player, T walker)
	{
		if(walker.getWorld().isClient())
			return false;
		
		if(walker.isParent(player))
		{
			walker.parentTo(null);
			walker.playSound(WHCSoundEvents.SEATBELT_OFF, 1F, 1F);
			return true;
		}
		else if((!walker.hasParent() || player.isCreative()) && walker.canParentToChild(player, walker))
		{
			walker.parentTo(player);
			walker.playSound(WHCSoundEvents.SEATBELT_ON, 1F, 1F);
			
			ServerEvents.ON_ENTITY_PARENT.invoker().onParentToEntity(player, walker);
			return true;
		}
		
		return false;
	}
}
