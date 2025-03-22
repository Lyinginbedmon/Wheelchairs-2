package com.lying.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemController<T extends Entity & ItemSteerable> extends Item
{
	private final RegistrySupplier<EntityType<T>> target;
	private final int damagePerUse;
	
	public ItemController(RegistrySupplier<EntityType<T>> target, int damagePerUse, Item.Settings settings)
	{
		super(settings);
		this.target = target;
		this.damagePerUse = damagePerUse;
	}
	
	public ActionResult use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(world.isClient)
			return ActionResult.PASS;
		else
		{
			Entity entity = user.getControllingVehicle();
			if(user.hasVehicle() && entity instanceof ItemSteerable itemSteerable && entity.getType() == this.target.get() && itemSteerable.consumeOnAStickItem())
			{
				EquipmentSlot equipmentSlot = LivingEntity.getSlotForHand(hand);
				ItemStack itemStack2 = itemStack.damage(this.damagePerUse, Items.FISHING_ROD, user, equipmentSlot);
				return ActionResult.SUCCESS_SERVER.withNewHandStack(itemStack2);
			}
			
			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return ActionResult.PASS;
		}
	}
}
