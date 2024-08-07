package com.lying.wheelchairs.utility;

import com.lying.wheelchairs.entity.IFlyingMount;
import com.lying.wheelchairs.network.StartFlyingPacket;
import com.lying.wheelchairs.renderer.entity.feature.CatVestLayer;
import com.lying.wheelchairs.renderer.entity.feature.FoxVestLayer;
import com.lying.wheelchairs.renderer.entity.feature.ParrotVestLayer;
import com.lying.wheelchairs.renderer.entity.feature.WolfVestLayer;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;

public class ClientBus
{
	@SuppressWarnings("unchecked")
	public static void registerEventCallbacks()
	{
		ServerEvents.ON_DOUBLE_JUMP.register((living) -> 
		{
			if(living instanceof IFlyingMount && ((IFlyingMount)living).canStartFlying())
				StartFlyingPacket.send();
		});
		
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> 
		{
			if(entityType == EntityType.WOLF)
				registrationHelper.register(new WolfVestLayer((FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>>)entityRenderer));
			else if(entityType == EntityType.CAT)
				registrationHelper.register(new CatVestLayer((FeatureRendererContext<CatEntity, CatEntityModel<CatEntity>>)entityRenderer));
			else if(entityType == EntityType.PARROT)
				registrationHelper.register(new ParrotVestLayer((FeatureRendererContext<ParrotEntity, ParrotEntityModel>)entityRenderer));
			else if(entityType == EntityType.FOX)
				registrationHelper.register(new FoxVestLayer((FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>>)entityRenderer));
		});
	}
}
