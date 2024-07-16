package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.entity.IParentedEntity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityList;

@Mixin(ClientWorld.class)
public class ClientWorldMixin extends WorldMixin
{
	@Shadow
	final EntityList entityList = new EntityList();
	
	@Shadow
	public void tickEntity(Entity entity) { }
	
	@Inject(method = "tickEntities()V", at = @At("HEAD"), cancellable = true)
	private void whc$tickEntities(final CallbackInfo ci)
	{
		ci.cancel();
		
		Profiler profiler = getProfiler();
		profiler.push("entities");
		entityList.forEach(entity -> {
			if(entity.isRemoved() || entity.hasVehicle() || entity instanceof IParentedEntity && ((IParentedEntity)entity).hasParent())
				return;
			tickEntity(this::tickEntity, entity);
		});
		profiler.pop();
		tickBlockEntities();
	}
	
	@Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	private void whc$tickEntity(Entity entity, final CallbackInfo ci)
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity parent = (LivingEntity)entity;
			parent.getWorld().getEntitiesByClass(LivingEntity.class, parent.getBoundingBox().expand(IParentedEntity.SEARCH_RANGE), IParentedEntity.isChildOf(parent))
				.forEach(child -> tickParented(parent, (LivingEntity & IParentedEntity)child));
		}
	}
	
	private <T extends LivingEntity & IParentedEntity> void tickParented(LivingEntity parent, T child)
	{
		if(parent.hasPassenger(child))
			return;
		
		child.resetPosition();
		++child.age;
		IParentedEntity.updateParentingBond(child, parent);
	}
}
