package com.lying.wheelchairs.network;

import com.lying.wheelchairs.reference.Reference;

import net.minecraft.util.Identifier;

public class WHCPacketHandler
{
	public static final Identifier OPEN_INVENTORY_ID	= make("open_inventory_screen");
	public static final Identifier FLYING_START_ID		= make("flying_start");
	public static final Identifier FLYING_ROCKET_ID		= make("flying_rocket");
	public static final Identifier PARENTED_MOVE_ID		= make("parented_entity_move");
	public static final Identifier PARENTED_INPUT_ID	= make("parented_entity_input");
	public static final Identifier FORCE_UNPARENT_ID	= make("force_unparent");
	
	private static Identifier make(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }

}
