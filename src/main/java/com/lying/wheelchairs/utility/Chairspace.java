package com.lying.wheelchairs.utility;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.Wheelchairs;
import com.lying.wheelchairs.entity.IParentedEntity;
import com.lying.wheelchairs.init.WHCChairspaceConditions;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Vec3d;
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
	
	/** Returns true if there is at least one entity in storage under the given UUID */
	public boolean hasEntityFor(UUID ownerID){ return storage.containsKey(ownerID) && !storage.get(ownerID).isEmpty(); }
	
	public void storeEntityInChairspace(Entity ent, UUID ownerID, ChairspaceCondition condition, Flag... flags)
	{
		if(ent == null || ent.getWorld().isClient()) return;
		
		NbtCompound data = new NbtCompound();
		ent.saveNbt(data);
		
		Map<ChairspaceCondition, List<RespawnData>> ownerMap = storage.getOrDefault(ownerID, new HashMap<>());
		List<RespawnData> listForCondition = ownerMap.getOrDefault(condition, Lists.newArrayList());
		listForCondition.add(RespawnData.of(ent, flags));
		ownerMap.put(condition, listForCondition);
		storage.put(ownerID, ownerMap);
		
		ent.discard();
		this.markDirty();
		Wheelchairs.LOGGER.info("Stored entity "+ent.getName().getString()+" in Chairspace with condition "+condition.registryName().toString()+" by "+ownerID.toString());
	}
	
	/** Respawns all associated entities across all applicable conditions (if any) */
	public void reactToEvent(Event<?> eventIn, Entity owner)
	{
		UUID uuid = owner.getUuid();
		WHCChairspaceConditions.getApplicable(eventIn).forEach(condition -> respawnForCondition(uuid, owner, condition));
	}
	
	/** Respawns all associated entities stored under the given condition */
	public void respawnForCondition(UUID ownerID, Entity owner, ChairspaceCondition condition)
	{
		// Do not fire if there is not an owner to spawn on, a world to spawn in, or the world is client-side
		if(owner == null || owner.getWorld() == null || owner.isSpectator() || owner.getWorld().isClient() || !hasEntityFor(owner.getUuid()) || !condition.isApplicable(owner))
			return;
		
		Map<ChairspaceCondition, List<RespawnData>> ownerMap = storage.getOrDefault(ownerID, new HashMap<>());
		if(!ownerMap.containsKey(condition)) return;
		
		List<RespawnData> entities = ownerMap.getOrDefault(condition, Lists.newArrayList());
		if(entities.isEmpty()) return;
		
		ServerWorld world = (ServerWorld)owner.getWorld();
		entities.forEach(entry -> condition.applyPostEffects(entry.respawn(owner, world)));
		
		ownerMap.remove(condition);
		storage.put(ownerID, ownerMap);
		this.markDirty();
	}
	
	/**
	 * Handles the respawning of a single stored entity, including mounting it to the owner if desired.<br>
	 * @author Lying
	 */
	private static class RespawnData
	{
		private final NbtCompound entityData;
		private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		
		public RespawnData(NbtCompound data, Flag... flags)
		{
			this.entityData = data;
			for(Flag flag : flags)
				if(!this.flags.contains(flag))
					this.flags.add(flag);
		}
		
		public static RespawnData of(Entity entity, Flag... flags)
		{
			NbtCompound data = new NbtCompound();
			entity.saveNbt(data);
			return new RespawnData(data, flags);
		}
		
		public NbtCompound writeToNbt()
		{
			NbtCompound data = new NbtCompound();
			data.put("Entity", entityData);
			
			NbtList list = new NbtList();
			this.flags.forEach(flag -> list.add(NbtString.of(flag.toString())));
			data.put("Flags", list);
			return data;
		}
		
		public static RespawnData readFromNbt(NbtCompound nbt)
		{
			NbtList list = nbt.getList("Flags", NbtElement.STRING_TYPE);
			List<Flag> flags = Lists.newArrayList();
			for(int i=0; i<list.size(); i++)
			{
				Flag flag = Flag.get(list.getString(i));
				if(flag != null)
					flags.add(flag);
			}
			return new RespawnData(nbt.getCompound("Entity"), flags.toArray(new Flag[0]));
		}
		
		@Nullable
		public Entity respawn(Entity owner, ServerWorld world)
		{
			Entity storedEntity = EntityType.loadEntityWithPassengers(entityData, world, entity -> {
				entity.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), owner.getPitch());
	            return entity;
	        });
			
			if(storedEntity != null)
			{
				Wheelchairs.LOGGER.info("Restored entity "+storedEntity.getName().getString()+" from Chairspace with owner "+owner.getName().getString());
				world.spawnEntity(storedEntity);
				
				if(flags.contains(Flag.MOUNT) && !owner.hasVehicle())
					owner.startRiding(storedEntity);
				
				if(flags.contains(Flag.PARENT) && storedEntity instanceof IParentedEntity && owner instanceof LivingEntity)
				{
					LivingEntity parent = (LivingEntity)owner;
					IParentedEntity child = (IParentedEntity)storedEntity;
					
					Vec3d offset = child.getParentOffset(parent, parent.getYaw(), parent.getPitch());
					storedEntity.updatePosition(parent.getX() + offset.getX(), parent.getY() + offset.getY(), parent.getZ() + offset.getY());
					child.parentTo(parent);
				}
			}
			return storedEntity;
		}
	}
	
	/** Specific post-respawn effects that should be applied to a specific stored entity when respawned */
	public static enum Flag implements StringIdentifiable
	{
		MOUNT,
		PARENT;
		
		public String asString() { return name().toString(); }
		
		@Nullable
		public static Flag get(String nameIn)
		{
			for(Flag flag : values())
				if(flag.name().equals(nameIn))
					return flag;
			return null;
		}
	}
}
