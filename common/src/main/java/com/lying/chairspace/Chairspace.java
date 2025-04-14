package com.lying.chairspace;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Wheelchairs;
import com.lying.entity.IParentedEntity;
import com.lying.init.WHCChairspaceConditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.architectury.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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
	public static final PersistentState.Type<Chairspace> TYPE = new PersistentState.Type<Chairspace>(
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
	
	public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup lookup)
	{
		NbtList set = new NbtList();
		storage.forEach((uuid,map) -> 
		{
			if(map.isEmpty()) return;
			
			NbtCompound compound = new NbtCompound();
			// UUID of the associated player
			compound.putUuid("ID", uuid);
			// Map of conditions to set of entities to respawn
			compound.put("Data", SerializedConditionListMap.CODEC.encodeStart(NbtOps.INSTANCE, map).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow());
			
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static Chairspace createFromNbt(NbtCompound nbt, WrapperLookup lookup)
	{
		Chairspace chairs = new Chairspace();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		
		chairs.storage.clear();
		Map<UUID, Map<ChairspaceCondition, List<RespawnData>>> dataSet = new HashMap<>();
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			UUID id = compound.getUuid("ID");
			Map<ChairspaceCondition, List<RespawnData>> dataEntry = SerializedConditionListMap.CODEC.parse(NbtOps.INSTANCE, compound.get("Data")).getOrThrow();
			dataSet.put(id, dataEntry);
		}
		chairs.storage = dataSet;
		return chairs;
	}
	
	/** Returns true if there is at least one entity in storage under the given UUID */
	public boolean hasEntityFor(UUID ownerID) { return storage.entrySet().stream().anyMatch(e -> e.getKey().equals(ownerID) && !e.getValue().isEmpty()); }
	
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
		Wheelchairs.LOGGER.info("# Stored entity {} in Chairspace with condition {} by {}", ent.getName().getString(), condition.registryName().toString(), ownerID.toString());
	}
	
	/** Respawns all associated entities across all applicable conditions (if any) */
	public void reactToEvent(Event<?> eventIn, Entity owner)
	{
		WHCChairspaceConditions.getApplicable(eventIn).forEach(condition -> respawnForCondition(owner.getUuid(), owner, condition));
	}
	
	/** Respawns all associated entities stored under the given condition */
	public void respawnForCondition(UUID ownerID, Entity owner, ChairspaceCondition condition)
	{
		// Do not fire if there is not an owner to spawn on, a world to spawn in, or the world is client-side
		if(
				owner == null || owner.isSpectator() || 
				owner.getWorld() == null || owner.getWorld().isClient() || 
				!hasEntityFor(owner.getUuid()) || !condition.isApplicable(owner))
			return;
		
		Map<ChairspaceCondition, List<RespawnData>> ownerMap = storage.getOrDefault(ownerID, new HashMap<>());
		ownerMap.entrySet().stream().filter(e -> e.getKey().equals(condition)).map(Entry::getValue).forEach(entities -> 
		{
			Wheelchairs.LOGGER.info(" # Respawning {} entities from Chairspace for {} under condition {}", entities.size(), owner.getUuid().toString(), condition.registryName().toString());
			
			ServerWorld world = (ServerWorld)owner.getWorld();
			entities.forEach(entry -> condition.applyPostEffects(entry.respawn(owner, world)));
			
			ownerMap.remove(condition);
			storage.put(ownerID, ownerMap);
			this.markDirty();
		});
	}
	
	/**
	 * Handles the respawning of a single stored entity, including mounting it to the owner if desired.<br>
	 * @author Lying
	 */
	public static record RespawnData(NbtCompound entityData, EnumSet<Flag> flags)
	{
		public static final Codec<RespawnData> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				NbtCompound.CODEC.fieldOf("Entity").forGetter(r -> r.entityData()),
				SerializedFlagSet.CODEC.fieldOf("Flags").forGetter(r -> r.flags))
				.apply(instance, RespawnData::new));
		public static final Codec<List<RespawnData>> LIST_CODEC	= CODEC.listOf();
		
		public static RespawnData of(Entity entity, Flag... flagsIn)
		{
			NbtCompound data = new NbtCompound();
			entity.saveNbt(data);
			EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
			for(Flag flag : flagsIn)
				flags.add(flag);
			return new RespawnData(data, flags);
		}
		
		public static <T> T encodeList(DynamicOps<T> ops, List<RespawnData> input)
		{
			return LIST_CODEC.encodeStart(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
		}
		
		public static <T> List<RespawnData> decodeList(DynamicOps<T> ops, T input)
		{
			return LIST_CODEC.parse(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
		}
		
		@Nullable
		public Entity respawn(Entity owner, ServerWorld world)
		{
			Entity storedEntity = EntityType.loadEntityWithPassengers(entityData(), world, SpawnReason.LOAD, entity -> {
				entity.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), owner.getPitch());
	            return entity;
	        });
			
			if(storedEntity != null)
			{
				Wheelchairs.LOGGER.info(" # - Restored entity {}", storedEntity.getName().getString());
				world.spawnEntity(storedEntity);
				flags().stream().forEach(f -> f.postRespawnAction.accept(owner, storedEntity));
			}
			return storedEntity;
		}
		
		private static class SerializedFlagSet
		{
			private static final Codec<EnumSet<Flag>> CODEC	= Codec.of(SerializedFlagSet::encode, SerializedFlagSet::decode);
			
			private static <T> DataResult<T> encode(final EnumSet<Flag> set, final DynamicOps<T> ops, final T prefix)
			{
				return (DataResult<T>)DataResult.success(ops.createList(set.stream().map(d -> ops.createString(d.asString()))));
			}
			
			private static <T> DataResult<Pair<EnumSet<Flag>, T>> decode(final DynamicOps<T> ops, final T input)
			{
				EnumSet<Flag> set = EnumSet.noneOf(Flag.class);
				set.addAll(ops.getStream(input).result().orElse(Stream.empty()).map(e -> Flag.get(ops.getStringValue(e).getOrThrow())).toList());
				return DataResult.success(Pair.of(set, input));
			}
		}
	}
	
	private static class SerializedConditionListMap
	{
		private static final Codec<Map<ChairspaceCondition, List<RespawnData>>> CODEC	= Codec.of(SerializedConditionListMap::encode, SerializedConditionListMap::decode);
		
		private static <T> DataResult<T> encode(final Map<ChairspaceCondition, List<RespawnData>> map, final DynamicOps<T> ops, final T prefix)
		{
			return DataResult.success(ops.createList(map.entrySet().stream().map(Entry::new).map(e -> e.encode(ops))));
		}
		
		private static <T> DataResult<Pair<Map<ChairspaceCondition, List<RespawnData>>, T>> decode(final DynamicOps<T> ops, final T input)
		{
			Map<ChairspaceCondition, List<RespawnData>> map = new HashMap<>();
			ops.getStream(input).result().get().map(t -> Entry.decode(ops, t)).forEach(e -> map.put(e.key(), e.list()));
			return DataResult.success(Pair.of(map, input));
		}
		
		private static record Entry(ChairspaceCondition key, List<RespawnData> list)
		{
			private static final Codec<Entry> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
					ChairspaceCondition.CODEC.fieldOf("Key").forGetter(Entry::key),
					RespawnData.LIST_CODEC.fieldOf("Value").forGetter(Entry::list))
						.apply(instance, Entry::new));
			
			public Entry(Map.Entry<ChairspaceCondition, List<RespawnData>> entryIn)
			{
				this(entryIn.getKey(), entryIn.getValue());
			}
			
			public <T> T encode(DynamicOps<T> ops)
			{
				return CODEC.encodeStart(ops, this).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
			}
			
			public static <T> Entry decode(DynamicOps<T> ops, T input)
			{
				return CODEC.parse(ops, input).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow();
			}
		}
	}
	
	/** Specific post-respawn effects that should be applied to a specific stored entity when respawned */
	public static enum Flag implements StringIdentifiable
	{
		MOUNT((owner, entity) -> { if(!owner.hasVehicle()) owner.startRiding(entity); }),
		PARENT((owner, entity) -> 
		{
			LivingEntity parent = (LivingEntity)owner;
			IParentedEntity child = (IParentedEntity)entity;
			
			Vec3d offset = child.getParentOffset(parent, parent.getYaw(), parent.getPitch());
			entity.updatePosition(parent.getX() + offset.getX(), parent.getY() + offset.getY(), parent.getZ() + offset.getY());
			child.parentTo(parent);
		});
		
		private final BiConsumer<Entity, Entity> postRespawnAction;
		
		private Flag(BiConsumer<Entity, Entity> consumerIn)
		{
			postRespawnAction = consumerIn;
		}
		
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
