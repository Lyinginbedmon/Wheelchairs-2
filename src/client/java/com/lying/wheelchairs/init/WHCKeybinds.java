package com.lying.wheelchairs.init;

import com.lying.wheelchairs.reference.Reference;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class WHCKeybinds
{
	public static KeyBinding keyOpenChair;
	
	public static KeyBinding make(String name, InputUtil.Type type, int standard)
	{
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key."+Reference.ModInfo.MOD_ID+"."+name,
				type,
				standard,
				"category."+Reference.ModInfo.MOD_ID+".keybindings"
				));
	}
}
