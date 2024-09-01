package com.lying.client.utility;

import java.util.List;
import java.util.function.Supplier;

import com.lying.Wheelchairs;
import com.lying.client.WheelchairsClient;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class AACLibrary
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public static PhraseSet BASIC;
	public static PhraseSet FEELINGS;
	public static PhraseSet PLACES;
	public static PhraseSet ACTIONS;
	public static PhraseSet THINGS;
	
	public static PhraseSet[] ALL_SETS;
	
	public static void init()
	{
		BASIC = new PhraseSet("basic", List.of(
				entrySimple("hello"),
				entrySimple("goodbye"),
				entrySimple("yes"),
				entrySimple("no"),
				entrySimple("okay"),
				entryDynamic(translate("my_coords_title"), () -> translate("my_coords", mc.player.getBlockPos().toShortString())),
				entryDynamic(translate("my_health_title"), () -> translate("my_health", String.valueOf((int)mc.player.getHealth()))),
				entryDynamic(translate("held_item_title"), () -> translate("held_item", mc.player.getOffHandStack().getName())),
				entrySimple("come_here"),
				entrySimple("stay_away"),
				entrySimple("on_my_way"),
				entrySimple("how_are_you"),
				entrySimple("you"),
				entrySimple("me"),
				entrySimple("need"),
				entrySimple("have"),
				entrySimple("please"),
				entrySimple("thank_you"),
				entrySimple("more"),
				entrySimple("less")));
		FEELINGS = new PhraseSet("feelings", List.of(
				entrySimple("good"),
				entrySimple("bad"),
				entrySimple("scared"),
				entrySimple("happy"),
				entrySimple("fun"),
				entrySimple("excited"),
				entrySimple("bored"),
				entrySimple("sad"),
				entrySimple("alone"),
				entrySimple("tired"),
				entrySimple("nervous"),
				entrySimple("confused"),
				entrySimple("understand"),
				entrySimple("hurt"),
				entrySimple("hungry"),
				entrySimple("danger"),
				entrySimple("safe")));
		PLACES = new PhraseSet("places", List.of(
				entrySimple("here"),
				entrySimple("there"),
				entrySimple("world_spawn"),
				entrySimple("base"),
				entrySimple("overworld"),
				entrySimple("the_end"),
				entrySimple("the_nether"),
				entrySimple("stronghold"),
				entrySimple("dungeon"),
				entrySimple("ocean"),
				entrySimple("desert"),
				entrySimple("forest"),
				entrySimple("field"),
				entrySimple("quarry"),
				entrySimple("caves"),
				entrySimple("ravine"),
				entrySimple("village"),
				entrySimple("farm"),
				entrySimple("spawner")));
		ACTIONS = new PhraseSet("actions", List.of(
				entrySimple("explore"),
				entrySimple("dig"),
				entrySimple("craft"),
				entrySimple("build"),
				entrySimple("cook"),
				entrySimple("enchant"),
				entrySimple("look"),
				entrySimple("going"),
				entrySimple("help"),
				entrySimple("eat"),
				entrySimple("find"),
				entrySimple("fight"),
				entrySimple("swim"),
				entrySimple("sail"),
				entrySimple("fly"),
				entrySimple("sleep"),
				entrySimple("follow"),
				entrySimple("stay"),
				entrySimple("defeat"),
				entrySimple("lost")));
		THINGS = new PhraseSet("things", List.of(
				entrySimple("food"),
				entrySimple("potion"),
				entrySimple("fish"),
				entrySimple("water"),
				entrySimple("lava"),
				entrySimple("bucket"),
				entrySimple("pickaxe"),
				entrySimple("axe"),
				entrySimple("shovel"),
				entrySimple("sword"),
				entrySimple("leather"),
				entrySimple("iron"),
				entrySimple("gold"),
				entrySimple("diamond"),
				entrySimple("armor"),
				entrySimple("helmet"),
				entrySimple("chestplate"),
				entrySimple("leggings"),
				entrySimple("boots"),
				entrySimple("monster"),
				entrySimple("animal"),
				entrySimple("trader"),
				entrySimple("pet"),
				entrySimple("boss"),
				entrySimple("dragon")));
		
		ALL_SETS = new PhraseSet[] 
				{
					BASIC,
					FEELINGS,
					PLACES,
					ACTIONS,
					THINGS
				};
		
		int total = 0;
		for(PhraseSet set : ALL_SETS) total += set.size();
		Wheelchairs.LOGGER.info("Initialised AAC library with "+total+" phrases across "+ALL_SETS.length+" sets");
		Wheelchairs.LOGGER.info("AAC narration setting: "+WheelchairsClient.config.shouldNarrateAAC());
	}
	
	private static MutableText translate(String name)
	{
		return Reference.ModInfo.translate("aac", name.toLowerCase()).copy();
	}
	
	private static MutableText translate(String name, Object... args)
	{
		return Reference.ModInfo.translate("aac", name.toLowerCase(), args).copy();
	}
	
	private static Pair<Text, Supplier<MutableText>> entrySimple(String name)
	{
		return entrySimple(() -> translate(name.toLowerCase()));
	}
	
	private static Pair<Text, Supplier<MutableText>> entrySimple(Supplier<MutableText> name)
	{
		return entryDynamic(name.get(), name);
	}
	
	private static Pair<Text, Supplier<MutableText>> entryDynamic(Text name, Supplier<MutableText> supplier)
	{
		return new Pair<Text, Supplier<MutableText>>(name, supplier);
	}
	
	public static class PhraseSet
	{
		public final String setName;
		private final Text displayName;
		private final List<Pair<Text,Supplier<MutableText>>> phrases;
		
		public PhraseSet(String setNameIn, List<Pair<Text, Supplier<MutableText>>> phrasesIn)
		{
			this(setNameIn, translate("set_"+setNameIn), phrasesIn);
		}
		
		public PhraseSet(String setNameIn, Text name, List<Pair<Text, Supplier<MutableText>>> phrasesIn)
		{
			setName = setNameIn;
			displayName = name;
			phrases = phrasesIn;
			
			if(phrases.size() > 25)
				Wheelchairs.LOGGER.warn("# AAC phrase set ["+setName+"] has more phrases than the AAC screen can display! "+phrases.size());
		}
		
		public Text displayName() { return this.displayName; }
		
		public int size() { return phrases.size(); }
		
		public Pair<Text, Supplier<MutableText>> get(int index) { return phrases.get(index); }
	}
}
