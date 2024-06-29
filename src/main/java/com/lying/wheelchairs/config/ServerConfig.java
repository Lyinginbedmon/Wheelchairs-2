package com.lying.wheelchairs.config;

import java.io.FileWriter;
import java.util.Properties;
import java.util.function.Predicate;

import com.lying.wheelchairs.data.WHCItemTags;
import com.lying.wheelchairs.init.WHCEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private SwordCaneFilter swordCanes = SwordCaneFilter.ENCHANT;
	
	private boolean handsyWalkers = false;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	public SwordCaneFilter swordCaneFilter() { return this.swordCanes; }
	
	public boolean handsyWalkers() { return this.handsyWalkers; }
	
	protected void readValues(Properties valuesIn)
	{
		swordCanes = SwordCaneFilter.get(parseStringOr(valuesIn.getProperty("SwordCaneFilter"), null));
		handsyWalkers = parseBoolOr(valuesIn.getProperty("HandsyWalkers"), false);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeString(writer, "SwordCaneFilter", swordCanes.asString());
		writeBool(writer, "HandsyWalkers", handsyWalkers);
	}
	
	static
	{
		DEFAULT_SETTINGS.put("SwordCaneFilter", SwordCaneFilter.ENCHANT.asString());
		DEFAULT_SETTINGS.put("HandsyWalkers", "FALSE");
	}
	
	public static enum SwordCaneFilter implements StringIdentifiable
	{
		ENCHANT(stack -> EnchantmentHelper.getLevel(WHCEnchantments.SLIM, stack) > 0),
		ALLOW_LIST(stack -> stack.isIn(WHCItemTags.FILTER_SWORD_CANE)),
		DENY_LIST(stack -> !SwordCaneFilter.ALLOW_LIST.test(stack));
		
		private final Predicate<ItemStack> condition;
		
		private SwordCaneFilter(Predicate<ItemStack> conditionIn)
		{
			this.condition = conditionIn;
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public final boolean test(ItemStack stack) { return !stack.isEmpty() && condition.test(stack); }
		
		public static SwordCaneFilter get(String name)
		{
			if(name != null)
				for(SwordCaneFilter value : values())
					if(value.asString().equalsIgnoreCase(name))
						return value;
			return ENCHANT;
		}
	}
}
