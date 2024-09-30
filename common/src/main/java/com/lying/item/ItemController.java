package com.lying.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemController<T extends Entity> extends Item
{
	private final RegistrySupplier<EntityType<T>> targetSupplier;
	private final int damagePerUse;
	
	public ItemController(Item.Settings settings, RegistrySupplier<EntityType<T>> targetIn, int damageIn)
	{
		super(settings);
		targetSupplier = targetIn;
		damagePerUse = damageIn;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(world.isClient)
			return TypedActionResult.pass(itemStack);
		
		Entity entity = user.getControllingVehicle();
		if(user.hasVehicle() && entity instanceof ItemSteerable)
		{
			ItemSteerable itemSteerable = (ItemSteerable)((Object)entity);
			if(entity.getType() == this.targetSupplier.get() && itemSteerable.consumeOnAStickItem())
			{
				itemStack.damage(this.damagePerUse, user, p -> p.sendToolBreakStatus(hand));
				if(itemStack.isEmpty())
				{
					ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
					itemStack2.setNbt(itemStack.getNbt());
					return TypedActionResult.success(itemStack2);
				}
				return TypedActionResult.success(itemStack);
			}
		}
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		return TypedActionResult.pass(itemStack);
	}
}
