package com.lying.neoforge;

import java.util.Optional;
import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.entity.EntityStool;
import com.lying.entity.EntityWalker;
import com.lying.entity.EntityWheelchair;
import com.lying.init.WHCEntityTypes;
import com.lying.item.ItemVest;
import com.lying.neoforge.capability.VestCapability;
import com.lying.reference.Reference;
import com.lying.utility.XPlatHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsNeoForge
{
	public static final EntityCapability<VestCapability, Void> VEST_DATA	= EntityCapability.createVoid(VestCapability.IDENTIFIER, VestCapability.class);
	
	public static Supplier<PlayerEntity> getLocalPlayer = () -> null;
	
    public WheelchairsNeoForge(IEventBus bus)
    {
		Wheelchairs.LOGGER.info("# Common init");
		Wheelchairs.commonInit();
		
		bus.addListener(this::registerEntityAttributes);
		
		Wheelchairs.HANDLER = new XPlatHandler()
		{
			private static Optional<VestCapability> getVestCap(LivingEntity entity)
			{
				return ItemVest.isValidMobForVest(entity) ? Optional.of(entity.getCapability(VEST_DATA)) : Optional.empty();
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
				if(!entity.getWorld().isClient())
					NeoForge.EVENT_BUS.post(new VestChangeEvent(entity, stack));
			}
		};
    }
	
	public void registerEntityAttributes(final EntityAttributeCreationEvent event)
	{
		Wheelchairs.LOGGER.info(" # Registered entity attributes");
		event.put(WHCEntityTypes.WHEELCHAIR.get(), EntityWheelchair.createWheelchairAttributes().build());
		event.put(WHCEntityTypes.WALKER.get(), EntityWalker.createWalkerAttributes().build());
		event.put(WHCEntityTypes.STOOL.get(), EntityStool.createStoolAttributes().build());
	}
}
