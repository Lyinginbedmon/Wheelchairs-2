package com.lying.client.init;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.lying.reference.Reference;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class WHCKeybinds
{
	private static final List<KeyBinding> KEYS = Lists.newArrayList();
	
	public static KeyBinding keyOpenChair = make("open_chair", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C);
	public static KeyBinding keySeatbelt = make("seatbelt", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X);
	
	public static KeyBinding make(String name, InputUtil.Type type, int standard)
	{
		KeyBinding binding = new KeyBinding(
				"key."+Reference.ModInfo.MOD_ID+"."+name,
				type,
				standard,
				"category."+Reference.ModInfo.MOD_ID+".keybindings"
				);
		KEYS.add(binding);
		return binding;
	}
	
	public static void init()
	{
		KEYS.forEach(KeyMappingRegistry::register);
	}
}
