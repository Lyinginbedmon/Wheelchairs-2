package com.lying.neoforge;

import java.util.Optional;
import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.entity.StoolEntity;
import com.lying.entity.WalkerEntity;
import com.lying.entity.WheelchairEntity;
import com.lying.init.WHCEntityTypes;
import com.lying.item.VestItem;
import com.lying.neoforge.capability.VestCapability;
import com.lying.reference.Reference;
import com.lying.utility.XPlatHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Reference.ModInfo.MOD_ID)
public final class WheelchairsNeoForge
{
	private static final DeferredRegister<TrackedDataHandler<?>> DATA_SERIALIZERS	= DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Reference.ModInfo.MOD_ID);
	public static final EntityCapability<VestCapability, Void> VEST_DATA	= EntityCapability.createVoid(VestCapability.IDENTIFIER, VestCapability.class);
	
	public static Supplier<PlayerEntity> getLocalPlayer = () -> null;
	
    public WheelchairsNeoForge(IEventBus bus)
    {
		Wheelchairs.LOGGER.info("# Common init");
		Wheelchairs.commonInit();
		DATA_SERIALIZERS.register("chair_upgrades", () -> WheelchairEntity.UPGRADE_LIST);
		DATA_SERIALIZERS.register(bus);
		
		bus.addListener(this::registerVestCapability);
		bus.addListener(this::registerEntityAttributes);
		NeoForge.EVENT_BUS.addListener(VestCapability::onLivingTick);
		
		Wheelchairs.HANDLER = new XPlatHandler()
		{
			private static Optional<VestCapability> getVestCap(LivingEntity entity)
			{
				if(!VestItem.isValidMobForVest(entity))
					return Optional.empty();
				VestCapability cap = entity.getCapability(VEST_DATA);
				return cap == null ? Optional.empty() : Optional.of(cap);
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
    
    public void registerVestCapability(RegisterCapabilitiesEvent event)
    {
    	VestItem.APPLICABLE_MOBS.keySet().forEach(type -> event.registerEntity(VEST_DATA, type, (e, c) -> e.getCapability(VEST_DATA)));
    }
	
	public void registerEntityAttributes(final EntityAttributeCreationEvent event)
	{
		Wheelchairs.LOGGER.info(" # Registered entity attributes");
		event.put(WHCEntityTypes.WHEELCHAIR.get(), WheelchairEntity.createWheelchairAttributes().build());
		event.put(WHCEntityTypes.WALKER.get(), WalkerEntity.createWalkerAttributes().build());
		event.put(WHCEntityTypes.STOOL.get(), StoolEntity.createStoolAttributes().build());
	}
}
