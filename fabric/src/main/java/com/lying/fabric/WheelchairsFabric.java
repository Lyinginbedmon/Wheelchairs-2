package com.lying.fabric;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class WheelchairsFabric implements ModInitializer
{
    public void onInitialize()
    {
    	Wheelchairs.commonInit();
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createMountAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes());
		FabricDefaultAttributeRegistry.register(WHCEntityTypes.STOOL.get(), EntityStool.createMountAttributes());
    }
}
