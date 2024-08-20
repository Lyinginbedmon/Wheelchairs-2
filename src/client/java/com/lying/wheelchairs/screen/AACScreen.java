package com.lying.wheelchairs.screen;

import java.util.List;
import java.util.function.Supplier;

import com.lying.wheelchairs.network.AACMessagePacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class AACScreen extends Screen
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	private static final int BUTTON_HEIGHT = 30, BUTTON_WIDTH = 80;
	private static final int SPACING = 5;
	private final int rowsTotal = 5, columnsTotal = 5;
	
	private PhraseSet[] phraseSets = new PhraseSet[] 
			{
				new PhraseSet(Text.literal("Basic"), List.of(
						entry("hello"),
						entry("goodbye"),
						entry("yes"),
						entry("no"),
						entry(translate("my_coords_title"), () -> Text.translatable("aac.wheelchairs.my_coords", mc.player.getBlockPos().toShortString())),
						entry("come_here"),
						entry("on_my_way"),
						entry("understand"),
						entry("dont_understand"),
						entry("monsters"))),
				new PhraseSet(Text.literal("Needs"), List.of(
						entry("help_me"),
						entry("help_you"),
						entry("want_build"),
						entry("want_craft"),
						entry("want_explore"),
						entry("want_sleep"),
						entry("hungry")))
			};
	private int currentSet = 0;
	
	private ButtonWidget[] setButtons = new ButtonWidget[2];
	private AACButton[] phraseButtons = new AACButton[rowsTotal * columnsTotal];
	
	private static MutableText translate(String name)
	{
		return Text.translatable("aac.wheelchairs."+name.toLowerCase());
	}
	
	private static Pair<Text, Supplier<MutableText>> entry(String name)
	{
		return entry(() -> translate(name.toLowerCase()));
	}
	
	private static Pair<Text, Supplier<MutableText>> entry(Supplier<MutableText> name)
	{
		return entry(name.get(), name);
	}
	
	private static Pair<Text, Supplier<MutableText>> entry(Text name, Supplier<MutableText> supplier)
	{
		return new Pair<Text, Supplier<MutableText>>(name, supplier);
	}
	
	public AACScreen(Text title)
	{
		super(title);
	}
	
	public void init()
	{
		super.init();
		
		int gridWidth = (BUTTON_WIDTH * columnsTotal) + (SPACING * (columnsTotal - 1));
		int gridHeight = (BUTTON_HEIGHT * rowsTotal) + (SPACING * (rowsTotal - 1));
		int left = (width - gridWidth) / 2;
		int top = (height - gridHeight) / 2;

		int shiftX = BUTTON_WIDTH + SPACING;
		int shiftY = BUTTON_HEIGHT + SPACING;
		for(int i=0; i<setButtons.length; i++)
		{
			final int index = i;
			PhraseSet set = phraseSets[i];
			addDrawableChild(setButtons[i] = ButtonWidget.builder(set.displayName(), button -> setPhraseSet(index)).dimensions(left + (i * shiftX), top - BUTTON_HEIGHT - SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		}
		
		for(int i=0; i<phraseButtons.length; i++)
		{
			int col = Math.floorDiv(i, columnsTotal);
			int row = i%rowsTotal;
			addDrawableChild(phraseButtons[i] = new AACButton(left + (shiftX * col), top + (shiftY * row), this));
		}
		
		updateButtons();
	}
	
	private void setPhraseSet(int index)
	{
		this.currentSet = Math.abs(index)%phraseSets.length;
		updateButtons();
	}
	
	private void updateButtons()
	{
		PhraseSet set = phraseSets[currentSet];
		for(int i=0; i<phraseButtons.length; i++)
		{
			if(i >= set.size())
			{
				phraseButtons[i].setPhrase(Text.empty(), () -> null);
				continue;
			}
			
			Pair<Text, Supplier<MutableText>> entry = set.get(i);
			phraseButtons[i].setPhrase(entry.getLeft(), entry.getRight());
		}
	}
	
	public boolean shouldPause() { return false; }
	
	private class AACButton extends ButtonWidget
	{
		private final Screen parentScreen;
		private Supplier<MutableText> message = Text::empty;
		
		protected AACButton(int x, int y, Screen parent)
		{
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Text.empty(), AACButton::onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			this.parentScreen = parent;
		}
		
		private void transmitMessage()
		{
			AACMessagePacket.send(message.get());
		}
		
		public void setPhrase(Text title, Supplier<MutableText> messageIn)
		{
			this.setMessage(title);
			this.message = messageIn;
		}
		
		public static void onPress(ButtonWidget button)
		{
			if(button instanceof AACButton)
			{
				AACButton phrase = (AACButton)button;
				if(phrase.message.get() != null)
					phrase.transmitMessage();
				
				phrase.parentScreen.close();
			}
		}
	}
	
	private class PhraseSet
	{
		private final Text displayName;
		private final List<Pair<Text,Supplier<MutableText>>> phrases;
		
		public PhraseSet(Text name, List<Pair<Text, Supplier<MutableText>>> phrasesIn)
		{
			displayName = name;
			phrases = phrasesIn;
		}
		
		public Text displayName() { return this.displayName; }
		
		public int size() { return phrases.size(); }
		
		public Pair<Text,Supplier<MutableText>> get(int index) { return phrases.get(index); }
	}
}
