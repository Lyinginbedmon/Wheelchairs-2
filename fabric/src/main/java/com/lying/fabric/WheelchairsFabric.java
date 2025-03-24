package com.lying.fabric;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.internal.base.ComponentRegistrationInitializer;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.WheelchairEntity;
import com.lying.fabric.component.VestComponent;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;
import com.lying.utility.XPlatHandler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public final class WheelchairsFabric implements ModInitializer, ComponentRegistrationInitializer
{
	public static final ComponentKey<VestComponent> VEST_DATA	= ComponentRegistry.getOrCreate(Reference.ModInfo.prefix("vest_data"), VestComponent.class);
	
    public void onInitialize()
    {
        Wheelchairs.commonInit();
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntity.createWheelchairAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.STOOL.get(), EntityStool.createStoolAttributes());
		
		Wheelchairs.HANDLER = new XPlatHandler()
		{
			public boolean hasVest(LivingEntity entity)
			{
				return ItemVest.isValidMobForVest(entity) && VEST_DATA.get(entity).hasVest();
			}
			
			public ItemStack getVest(LivingEntity entity)
			{
				return !ItemVest.isValidMobForVest(entity) ? ItemStack.EMPTY : VEST_DATA.get(entity).get();
			}
			
			public void setVest(LivingEntity entity, ItemStack stack)
			{
				if(ItemVest.isValidMobForVest(entity))
					VEST_DATA.get(entity).setVest(stack);
			}
		};
    }
}
