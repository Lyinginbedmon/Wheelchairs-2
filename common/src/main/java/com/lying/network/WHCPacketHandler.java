package com.lying.network;

import com.lying.reference.Reference;

import net.minecraft.util.Identifier;

public class WHCPacketHandler
{
	public static final Identifier OPEN_INVENTORY_ID	= make("open_inventory_screen");
	public static final Identifier FLYING_START_ID	= make("flying_start");
	public static final Identifier FLYING_ROCKET_ID	= make("flying_rocket");
	
	private static Identifier make(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }

}
