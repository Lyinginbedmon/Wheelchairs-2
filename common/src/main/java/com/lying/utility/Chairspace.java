package com.lying.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class Chairspace extends PersistentState
{
	public static final String ID = "chairspace";
	public static final PersistentState.Type<Chairspace> TYPE = new Type<>(
			Chairspace::new,
            Chairspace::createFromNbt,
            null
    );
	
	private Map<UUID, NbtCompound> storage = new HashMap<>();
	
	public static Chairspace getChairspace(MinecraftServer server)
	{
		ServerWorld world = server.getWorld(World.OVERWORLD);
		PersistentStateManager manager = world.getPersistentStateManager();
		Chairspace chairs = manager.getOrCreate(TYPE, ID);
		chairs.markDirty();
		return chairs;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		NbtList set = new NbtList();
		storage.forEach((tricksy,power) -> 
		{
			NbtCompound compound = new NbtCompound();
			compound.putUuid("ID", tricksy);
			compound.put("Chair", power);
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static Chairspace createFromNbt(NbtCompound nbt)
	{
		Chairspace chairs = new Chairspace();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			chairs.storage.put(compound.getUuid("ID"), compound.getCompound("Chair"));
		}
		return chairs;
	}
	
	public boolean hasChairFor(UUID ownerID) { return storage.containsKey(ownerID); }
	
	public void storeChair(Entity ent, UUID ownerID)
	{
		NbtCompound data = new NbtCompound();
		ent.saveNbt(data);
		storage.put(ownerID, data);
		ent.discard();
		this.markDirty();
	}
	
	public void tryRespawnChair(UUID ownerID, Entity owner)
	{
		if(owner == null || !storage.containsKey(ownerID) || owner.isSpectator() || !owner.isAlive() || owner.getWorld() == null || owner.getWorld().isClient())
			return;
		
		ServerWorld world = (ServerWorld)owner.getWorld();
		Entity chair = EntityType.loadEntityWithPassengers(storage.get(ownerID), world, entity -> {
			entity.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), owner.getPitch());
            return entity;
        });
		
		if(chair != null)
		{
			world.spawnEntity(chair);
			owner.startRiding(chair);
		}
		
		storage.remove(ownerID);
		this.markDirty();
	}
}
