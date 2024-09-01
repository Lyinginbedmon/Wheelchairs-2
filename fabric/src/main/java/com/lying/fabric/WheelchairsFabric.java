package com.lying.fabric;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.fabric.component.VestComponent;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;
import com.lying.utility.XPlatHandler;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class WheelchairsFabric implements ModInitializer, EntityComponentInitializer
{
	public static final ComponentKey<VestComponent> VEST_DATA	= ComponentRegistry.getOrCreate(new Identifier(Reference.ModInfo.MOD_ID, "vest_data"), VestComponent.class);
	
    public void onInitialize()
    {
    	Wheelchairs.commonInit();
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createMountAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.STOOL.get(), EntityStool.createMountAttributes());
		
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
    
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
    {
    	registry.registerFor(WolfEntity.class, VEST_DATA, VestComponent::new);
    	registry.registerFor(CatEntity.class, VEST_DATA, VestComponent::new);
    	registry.registerFor(ParrotEntity.class, VEST_DATA, VestComponent::new);
    	registry.registerFor(FoxEntity.class, VEST_DATA, VestComponent::new);
    }
}
