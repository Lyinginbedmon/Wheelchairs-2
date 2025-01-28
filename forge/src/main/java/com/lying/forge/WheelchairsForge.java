package com.lying.forge;

import java.util.Optional;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.forge.capability.VestCapability;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.reference.Reference;
import com.lying.utility.XPlatHandler;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsForge
{
	public static final Capability<VestCapability> VEST_DATA	= CapabilityManager.get(new CapabilityToken<>() { });
	
	public WheelchairsForge()
	{
		EventBuses.registerModEventBus(Reference.ModInfo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		Wheelchairs.LOGGER.info("# Common init");
		Wheelchairs.commonInit();
		
		final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::registerEntityAttributes);
		eventBus.addListener(this::registerVestCapability);
		MinecraftForge.EVENT_BUS.addListener(VestCapability::onLivingTick);
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, WheelchairsForge::attachVestCapability);
		
		Wheelchairs.HANDLER = new XPlatHandler()
		{
			private static Optional<VestCapability> getVestCap(LivingEntity entity)
			{
				return ItemVest.isValidMobForVest(entity) ? entity.getCapability(VEST_DATA).resolve() : Optional.empty();
			}
			
			public boolean hasVest(LivingEntity entity)
			{
				Optional<VestCapability> opt = getVestCap(entity);
				return opt.isPresent() && opt.get().hasVest();
			}
			
			public ItemStack getVest(LivingEntity entity)
			{
				Optional<VestCapability> opt = getVestCap(entity);
				return opt.isPresent() ? opt.get().get() : ItemStack.EMPTY;
			}
			
			public void setVest(LivingEntity entity, ItemStack stack)
			{
				getVestCap(entity).ifPresent(cap -> cap.setVest(stack));
			}
		};
	}
	
	public void registerEntityAttributes(final EntityAttributeCreationEvent event)
	{
		Wheelchairs.LOGGER.info(" # Registered entity attributes");
		event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createMountAttributes().build());
		event.put(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes().build());
		event.put(WHCEntityTypes.STOOL.get(), EntityStool.createMountAttributes().build());
	}
	
	public void registerVestCapability(final RegisterCapabilitiesEvent event)
	{
		Wheelchairs.LOGGER.info(" # Registered vest capability");
		event.register(VestCapability.class);
	}
	
	public static void attachVestCapability(final AttachCapabilitiesEvent<Entity> event)
	{
		Entity e = event.getObject();
		if(e instanceof LivingEntity && ItemVest.isValidMobForVest((LivingEntity)e))
			event.addCapability(VestCapability.IDENTIFIER, new VestCapability((LivingEntity)event.getObject()));
	}
}
