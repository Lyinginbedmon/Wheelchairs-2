package com.lying.item;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.lying.Wheelchairs;
import com.lying.mixin.FoxEntityMixin;

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
	public static final Map<EntityType<? extends LivingEntity>, Function<Entity,UUID>> APPLICABLE_MOBS = Map.of(
			EntityType.WOLF, tamedOwner,
			EntityType.CAT, tamedOwner,
			EntityType.PARROT, tamedOwner,
			EntityType.FOX, entity -> ((FoxEntityMixin)(Object)entity).getOwnerID().orElse(null));
	
	public ItemVest(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand)
	{
		if(!isValidMobForVest(entity))
			return ActionResult.PASS;
		
		UUID ownerID = getVestedMobOwner(entity);
		if(ownerID == null || ownerID != user.getUuid())
			return ActionResult.PASS;
		
		ItemStack vest = getVest(entity);
		if(!vest.isEmpty())
			dropVest(entity);
		
		setVest(entity, stack.split(1));
		user.sendMessage(Text.translatable("gui.wheelchairs.service_vest_applied"), true);
		if(!user.isCreative())
			stack.decrement(1);
		return ActionResult.success(user.getWorld().isClient());
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
		return Wheelchairs.HANDLER.getVest(entity);
	}
	
	public static void setVest(LivingEntity entity, ItemStack stack)
	{
		Wheelchairs.HANDLER.setVest(entity, stack);
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
