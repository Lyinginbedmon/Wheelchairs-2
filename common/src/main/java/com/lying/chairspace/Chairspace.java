package com.lying.chairspace;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
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
	
	private List<PlayerStorage> storage = Lists.newArrayList();
	
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
		storage.removeIf(PlayerStorage::isEmpty);
		nbt.put("Data", PlayerStorage.LIST_CODEC.encodeStart(NbtOps.INSTANCE, storage).resultOrPartial(Wheelchairs.LOGGER::error).get());
		return nbt;
	}
	
	public static Chairspace createFromNbt(NbtCompound nbt, WrapperLookup lookup)
	{
		Chairspace chairs = new Chairspace();
		chairs.storage.clear();
		chairs.storage.addAll(PlayerStorage.LIST_CODEC.parse(NbtOps.INSTANCE, nbt.get("Data")).resultOrPartial(Wheelchairs.LOGGER::error).orElseThrow());
		return chairs;
	}
	
	/** Returns true if there is at least one entity in storage under the given UUID */
	public boolean hasEntityFor(UUID ownerID) { return storage.stream().anyMatch(s -> s.playerID().equals(ownerID) && !s.isEmpty()); }
	
	public void storeEntityInChairspace(Entity ent, UUID ownerID, ChairspaceCondition condition, Flag... flags)
	{
		if(ent == null || ent.getWorld().isClient()) return;
		
		RespawnData entry = RespawnData.of(ent, flags);
		Predicate<PlayerStorage> predicate = s -> s.playerID().equals(ownerID);
		if(storage.stream().anyMatch(predicate))
			storage.stream().filter(predicate).forEach(s -> s.add(condition, entry));
		else
			storage.add(new PlayerStorage(ownerID).add(condition, entry));
		
		ent.discard();
		this.markDirty();
		Wheelchairs.LOGGER.info(" # Stored entity {} in Chairspace with condition {} by {}", ent.getName().getString(), condition.registryName().toString(), ownerID.toString());
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
		
		ServerWorld world = (ServerWorld)owner.getWorld();
		List<PlayerStorage> wares = storage.stream().filter(s -> s.playerID().equals(ownerID)).toList();
		for(PlayerStorage w : wares)
			if(w.respawnFor(condition, owner, world))
				markDirty();
		storage.removeIf(PlayerStorage::isEmpty);
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
		
		public static RespawnData of(Entity entity, Flag... flagsIn)
		{
			NbtCompound data = new NbtCompound();
			entity.saveNbt(data);
			EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
			for(Flag flag : flagsIn)
				flags.add(flag);
			return new RespawnData(data, flags);
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
	
	/**
	 * Serializable per-player condition to entities mapping
	 * @author Lying
	 */
	private static class PlayerStorage
	{
		public static final Codec<PlayerStorage> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(p -> p.playerID().toString()),
				ConditionEntry.CODEC.listOf().fieldOf("entries").forGetter(p -> p.entries))
				.apply(instance, (id,entries) -> 
				{
					PlayerStorage storage = new PlayerStorage(UUID.fromString(id));
					entries.forEach(storage::add);
					return storage;
				}));
		public static final Codec<List<PlayerStorage>> LIST_CODEC	= CODEC.listOf();
		
		private final UUID playerID;
		private final List<ConditionEntry> entries = Lists.newArrayList();
		
		public PlayerStorage(UUID idIn)
		{
			playerID = idIn;
		}
		
		public boolean equals(Object obj) { return obj instanceof PlayerStorage && ((PlayerStorage)obj).playerID().equals(playerID); }
		
		public UUID playerID() { return this.playerID; }
		
		public boolean isEmpty() { return entries.isEmpty() || entries.stream().allMatch(ConditionEntry::isEmpty); }
		
		public PlayerStorage add(ConditionEntry entry)
		{
			if(entries.stream().noneMatch(e -> e.equals(entry)))
				entries.add(entry);
			else
				entries.stream().filter(e -> e.equals(entry)).findFirst().ifPresent(e -> e.add(entry));
			
			return this;
		}
		
		public PlayerStorage add(ChairspaceCondition condition, RespawnData data)
		{
			Predicate<ConditionEntry> predicate = ConditionEntry.matching(condition);
			if(entries.stream().noneMatch(predicate))
				entries.add(new ConditionEntry(condition).add(data));
			else
				entries.stream().filter(predicate).findFirst().ifPresent(e -> e.add(data));
			
			return this;
		}
		
		public boolean respawnFor(ChairspaceCondition condition, Entity owner, ServerWorld world)
		{
			Predicate<ConditionEntry> predicate = ConditionEntry.matching(condition);
			if(entries.stream().anyMatch(predicate))
			{
				entries.stream().filter(predicate).forEach(entry -> entry.respawn(owner, world));
				entries.removeIf(e -> e.matches(condition));
				return true;
			}
			return false;
		}
		
		/**
		 * Serializable entry in {@link PlayerStorage}
		 * @author Lying
		 */
		private static class ConditionEntry
		{
			public static final Codec<ConditionEntry> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
					ChairspaceCondition.CODEC.fieldOf("condition").forGetter(ConditionEntry::condition),
					RespawnData.CODEC.listOf().fieldOf("objects").forGetter(ConditionEntry::entries))
					.apply(instance, (condition,entries) -> 
					{
						ConditionEntry entry = new ConditionEntry(condition);
						entries.forEach(entry::add);
						return entry;
					}));
			
			private final ChairspaceCondition condition;
			private final List<RespawnData> entries = Lists.newArrayList();
			
			public static Predicate<ConditionEntry> matching(ChairspaceCondition c) { return a -> a.matches(c); }
			
			public ConditionEntry(ChairspaceCondition conditionIn)
			{
				condition = conditionIn;
			}
			
			public boolean equals(Object obj) { return obj instanceof ConditionEntry && matches(((ConditionEntry)obj).condition()); }
			
			public boolean matches(ChairspaceCondition cond) { return cond.equals(condition); }
			
			public ChairspaceCondition condition() { return this.condition; }
			
			public List<RespawnData> entries() { return this.entries; }
			
			public boolean isEmpty() { return this.entries.isEmpty(); }
			
			public ConditionEntry add(RespawnData data)
			{
				entries.add(data);
				return this;
			}
			
			public ConditionEntry add(ConditionEntry other)
			{
				if(other.condition().equals(condition))
					entries.addAll(entries);
				return this;
			}
			
			public void respawn(Entity owner, ServerWorld world)
			{
				Wheelchairs.LOGGER.info(" # Respawning {} entities from Chairspace for {} under condition {}", entries.size(), owner.getUuid().toString(), condition.registryName().toString());
				entries.forEach(entry -> condition.applyPostEffects(entry.respawn(owner, world)));
				entries.clear();
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
