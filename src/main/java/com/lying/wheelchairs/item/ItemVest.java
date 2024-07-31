package com.lying.wheelchairs.item;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.init.WHCComponents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ItemVest extends Item implements DyeableItem
{
	private static final Function<Entity, UUID> tamedOwner = entity -> ((TameableEntity)entity).getOwnerUuid();
	public static final Map<EntityType<?>, Function<Entity,UUID>> APPLICABLE_MOBS = Map.of(
			EntityType.WOLF, tamedOwner,
			EntityType.CAT, tamedOwner,
			EntityType.OCELOT, tamedOwner,
			EntityType.PARROT, tamedOwner);
	
	public ItemVest(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand)
	{
		if(isValidMobForVest(entity))
		{
			ItemStack vest = getVest(entity);
			if(!vest.isEmpty())
				dropVest(entity);
			
			setVest(entity, stack.split(1));
			user.sendMessage(Text.literal("Time to put on your jacket!"), true);
			if(!user.isCreative())
				stack.decrement(1);
			return ActionResult.success(user.getWorld().isClient());
		}
		return ActionResult.PASS;
	}
	
	public static boolean isValidMobForVest(Entity entity)
	{
		return APPLICABLE_MOBS.containsKey(entity.getType());
	}
	
	@Nullable
	public static UUID getVestedMobOwner(Entity entity)
	{
		return APPLICABLE_MOBS.getOrDefault(entity.getType(), ent -> null).apply(entity);
	}
	
	public static ItemStack getVest(LivingEntity entity)
	{
		return WHCComponents.VEST_TRACKING.get(entity).get();
	}
	
	public static void setVest(LivingEntity entity, ItemStack stack)
	{
		WHCComponents.VEST_TRACKING.get(entity).setVest(stack);
	}
	
	public static void dropVest(LivingEntity entity)
	{
		ItemStack vest = getVest(entity);
		if(!vest.isEmpty())
		{
			entity.dropStack(vest);
			setVest(entity, ItemStack.EMPTY);
		}
	}
}
