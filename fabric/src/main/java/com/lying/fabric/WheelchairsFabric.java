package com.lying.fabric;

import org.ladysnake.cca.internal.base.ComponentRegistrationInitializer;

import com.lying.Wheelchairs;
import com.lying.entity.StoolEntity;
import com.lying.entity.WalkerEntity;
import com.lying.entity.WheelchairEntity;
import com.lying.fabric.init.WHCComponents;
import com.lying.init.WHCEntityTypes;
import com.lying.item.VestItem;
import com.lying.utility.XPlatHandler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;

public final class WheelchairsFabric implements ModInitializer, ComponentRegistrationInitializer
{
    public void onInitialize()
    {
        Wheelchairs.commonInit();
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntity.createWheelchairAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WALKER.get(), WalkerEntity.createWalkerAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.STOOL.get(), StoolEntity.createStoolAttributes());
		TrackedDataHandlerRegistry.register(WheelchairEntity.UPGRADE_LIST);
		
		Wheelchairs.HANDLER = new XPlatHandler()
		{
			public boolean hasVest(LivingEntity entity)
			{
				return VestItem.isValidMobForVest(entity) && WHCComponents.VEST_TRACKING.get(entity).hasVest();
			}
			
			public ItemStack getVest(LivingEntity entity)
			{
				return !VestItem.isValidMobForVest(entity) ? ItemStack.EMPTY : WHCComponents.VEST_TRACKING.get(entity).get();
			}
			
			public void setVest(LivingEntity entity, ItemStack stack)
			{
				if(VestItem.isValidMobForVest(entity))
					WHCComponents.VEST_TRACKING.get(entity).setVest(stack);
			}
		};
    }
}
