package com.lying.wheelchairs.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.init.WHCChairspaceConditions;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

/**
 * Chairspace is an extradimensional persistent storage for entities.<br>
 * Entities are stored associated with a UUID and condition and respawned when appropriate.<br>
 * @author Lying
 *
 */
public class Chairspace extends PersistentState
{
	public static final String ID = "chairspace";
	public static final PersistentState.Type<Chairspace> TYPE = new Type<>(
			Chairspace::new,
            Chairspace::createFromNbt,
            null
    );
	
	private Map<UUID, Map<ChairspaceCondition, List<RespawnData>>> storage = new HashMap<>();
	
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
		storage.forEach((uuid,map) -> 
		{
			if(map.isEmpty()) return;
			
			NbtCompound compound = new NbtCompound();
			// UUID of the associated player
			compound.putUuid("ID", uuid);
			
			// Map of conditions to set of entities to respawn
			NbtList mapData = new NbtList();
			map.forEach((condition, list) -> 
			{
				if(list.isEmpty()) return;
				
				NbtCompound entry = new NbtCompound();
				entry.putString("Condition", condition.registryName().toString());
				
				NbtList entries = new NbtList();
				list.forEach(respawn -> entries.add(respawn.writeToNbt()));
				entry.put("Entries", entries);
				
				mapData.add(entry);
			});
			compound.put("Data", mapData);
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static Chairspace createFromNbt(NbtCompound nbt)
	{
		Chairspace chairs = new Chairspace();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		
		chairs.storage.clear();
		Map<UUID, Map<ChairspaceCondition, List<RespawnData>>> dataSet = new HashMap<>();
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			UUID id = compound.getUuid("ID");
			
			Map<ChairspaceCondition, List<RespawnData>> dataEntry = new HashMap<>();
			NbtList mapData = compound.getList("Data", NbtElement.COMPOUND_TYPE);
			for(int j=0; j<mapData.size(); j++)
			{
				NbtCompound entry = mapData.getCompound(j);
				ChairspaceCondition dataCondition = WHCChairspaceConditions.get(new Identifier(entry.getString("Condition")));
				if(dataCondition == null) continue;
				
				NbtList entries = entry.getList("Entries", NbtElement.COMPOUND_TYPE);
				if(entries.isEmpty()) continue;
				
				List<RespawnData> dataEntries = Lists.newArrayList();
				for(int k=0; k<entries.size(); k++)
					dataEntries.add(RespawnData.readFromNbt(entries.getCompound(k)));
				
				dataEntry.put(dataCondition, dataEntries);
			}
			
			dataSet.put(id, dataEntry);
		}
		chairs.storage = dataSet;
		return chairs;
	}
	
	public boolean hasEntityFor(UUID ownerID) { return storage.containsKey(ownerID) && !storage.get(ownerID).isEmpty(); }
	
	public void storeEntityInChairspace(Entity ent, UUID ownerID, ChairspaceCondition condition, boolean mountOnRespawn)
	{
		NbtCompound data = new NbtCompound();
		ent.saveNbt(data);
		
		Map<ChairspaceCondition, List<RespawnData>> ownerMap = storage.getOrDefault(ownerID, new HashMap<>());
		List<RespawnData> listForCondition = ownerMap.getOrDefault(condition, Lists.newArrayList());
		listForCondition.add(RespawnData.of(ent, mountOnRespawn));
		ownerMap.put(condition, listForCondition);
		storage.put(ownerID, ownerMap);
		
		ent.discard();
		this.markDirty();
	}
	
	public void respawnForCondition(UUID ownerID, Entity owner, ChairspaceCondition condition)
	{
		if(owner == null || !storage.containsKey(ownerID) || owner.isSpectator() || owner.getWorld() == null || owner.getWorld().isClient() || !condition.isApplicable(owner)) return;
		
		Map<ChairspaceCondition, List<RespawnData>> ownerMap = storage.getOrDefault(ownerID, new HashMap<>());
		if(!ownerMap.containsKey(condition)) return;
		
		List<RespawnData> entities = ownerMap.getOrDefault(condition, Lists.newArrayList());
		if(entities.isEmpty()) return;
		
		ServerWorld world = (ServerWorld)owner.getWorld();
		entities.forEach(respawn -> respawn.respawn(owner, world));
		
		ownerMap.remove(condition);
		storage.put(ownerID, ownerMap);
		this.markDirty();
	}
	
	/**
	 * Defines the context of an entity being respawned.
	 * @author Lying
	 */
	public static class ChairspaceCondition
	{
		private final Identifier registryName;
		
		private final Predicate<Entity> canApplyTo;
		
		private ChairspaceCondition(Identifier nameIn, Predicate<Entity> qualifierIn)
		{
			registryName = nameIn;
			canApplyTo = qualifierIn;
		}
		
		public Identifier registryName() { return this.registryName; }
		
		public boolean isApplicable(Entity player) { return this.canApplyTo.test(player); }
		
		public static class Builder
		{
			private final Identifier regName;
			private Predicate<Entity> canApplyTo = player -> player.isAlive();
			
			private Builder(Identifier regNameIn)
			{
				regName = regNameIn;
			}
			
			public static Builder of(String nameIn) { return new Builder(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
			
			public Builder condition(Predicate<Entity> conditionIn)
			{
				this.canApplyTo = conditionIn.and(canApplyTo);
				return this;
			}
			
			public ChairspaceCondition build() { return new ChairspaceCondition(regName, canApplyTo); }
		}
	}
	
	/**
	 * Handles the respawning of a single stored entity, including mounting it to the owner if desired.<br>
	 * @author Lying
	 */
	private static class RespawnData
	{
		private final NbtCompound entityData;
		private final boolean shouldMount;
		
		public RespawnData(NbtCompound data, boolean mount)
		{
			this.entityData = data;
			this.shouldMount = mount;
		}
		
		public static RespawnData of(Entity entity, boolean mount)
		{
			NbtCompound data = new NbtCompound();
			entity.saveNbt(data);
			return new RespawnData(data, mount);
		}
		
		public NbtCompound writeToNbt()
		{
			NbtCompound data = new NbtCompound();
			data.put("Entity", entityData);
			data.putBoolean("Mount", shouldMount);
			return data;
		}
		
		public static RespawnData readFromNbt(NbtCompound nbt)
		{
			return new RespawnData(nbt.getCompound("Entity"), nbt.getBoolean("Mount"));
		}
		
		public void respawn(Entity owner, ServerWorld world)
		{

			Entity storedEntity = EntityType.loadEntityWithPassengers(entityData, world, entity -> {
				entity.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), owner.getPitch());
	            return entity;
	        });
			
			if(storedEntity != null)
			{
				world.spawnEntity(storedEntity);
				
				if(shouldMount && !owner.hasVehicle())
					owner.startRiding(storedEntity);
			}
		}
	}
}
