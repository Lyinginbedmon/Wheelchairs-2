package com.lying.wheelchairs.init;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.wheelchairs.reference.Reference;
import com.lying.wheelchairs.utility.Chairspace.ChairspaceCondition;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Chairspace conditions define the circumstances under which an entity was stored in Chairspace.<br>
 * They then also define the conditions under which the entity can be respawned from Chairspace.<br>
 * @author Lying
 *
 */
public class WHCChairspaceConditions
{
	public static final RegistryKey<Registry<ChairspaceCondition>> KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "chairspace_condition"));
	public static final Registry<ChairspaceCondition> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();
	
	private static final List<ChairspaceCondition> CONDITIONS = Lists.newArrayList();
	
	/** Checked whenever the player respawns */
	public static final ChairspaceCondition ON_RESPAWN = register(ChairspaceCondition.Builder.of("on_respawn").shouldMount());
	
	/** Checked whenever the server receives a teleport confirmation packet */
	public static final ChairspaceCondition ON_FINISH_TELEPORT = register(ChairspaceCondition.Builder.of("on_finish_teleport").shouldMount());
	
	/** Checked whenever the player changes game mode */
	public static final ChairspaceCondition ON_GAMEMODE_CHANGE = register(ChairspaceCondition.Builder.of("on_corporeal").condition(player -> !player.isSpectator()).shouldMount());
	
	private static ChairspaceCondition register(ChairspaceCondition.Builder builder)
	{
		ChairspaceCondition made = builder.build();
		CONDITIONS.add(made);
		return made;
	}
	
	public static void init()
	{
		CONDITIONS.forEach(acc -> Registry.register(REGISTRY, acc.registryName(), acc));
	}
	
	@Nullable
	public static ChairspaceCondition get(Identifier nameIn) { return REGISTRY.get(nameIn); }
}
