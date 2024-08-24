package com.lying.wheelchairs.screen;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.wheelchairs.network.AACMessagePacket;
import com.lying.wheelchairs.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
	private static final int ROW_COUNT = 5, COL_COUNT = 5;
	private static final PhraseSet[] PHRASE_SETS;
	
	private Mode messageMode = Mode.SINGLE;
	private MutableText messageToSend = null;
	
	private int currentSet = 0;
	
	private ButtonWidget[] setButtons = new ButtonWidget[PHRASE_SETS.length];
	private AACButton[] phraseButtons = new AACButton[ROW_COUNT * COL_COUNT];
	private ButtonWidget sendButton;
	
	public AACScreen(Text title)
	{
		super(title);
	}
	
	public void init()
	{
		super.init();
		
		int gridWidth = (BUTTON_WIDTH * COL_COUNT) + (SPACING * (COL_COUNT - 1));
		int left = (width - gridWidth) / 2;
		int right = (width + gridWidth) / 2;
		
		generateSetButtons();
		generatePhraseButtonGrid();
		
		addDrawableChild(ButtonWidget.builder(Text.empty(), button -> setMode(messageMode == Mode.SINGLE ? Mode.COMPOUND : Mode.SINGLE)).dimensions(left, 220, 20, 20).build());
		addDrawableChild(sendButton = ButtonWidget.builder(Reference.ModInfo.translate("gui", "send_aac_message"), button -> send(messageToSend)).dimensions(right - 35, 220, 35, 20).build());
		
		setMode(Mode.SINGLE);
		setPhraseSet(0);
	}
	
	private void generateSetButtons()
	{
		int gridWidth = (BUTTON_WIDTH * COL_COUNT) + (SPACING * (COL_COUNT - 1));
		int setShift = BUTTON_WIDTH + SPACING;
		int left = (width - gridWidth) / 2;
		for(int i=0; i<PHRASE_SETS.length; i++)
		{
			final int index = i;
			PhraseSet set = PHRASE_SETS[i];
			addDrawableChild(setButtons[i] = ButtonWidget.builder(set.displayName(), button -> setPhraseSet(index)).dimensions(
					left + (setShift * i), 
					25, 
					BUTTON_WIDTH, 
					20).build());
		}
	}
	
	private void generatePhraseButtonGrid()
	{
		int gridWidth = (BUTTON_WIDTH * COL_COUNT) + (SPACING * (COL_COUNT - 1));
		int left = (width - gridWidth) / 2;
		int shiftX = BUTTON_WIDTH + SPACING;
		int shiftY = BUTTON_HEIGHT + SPACING;
		for(int i=0; i<phraseButtons.length; i++)
		{
			int col = i%COL_COUNT;
			int row = Math.floorDiv(i, ROW_COUNT);
			addDrawableChild(phraseButtons[i] = new AACButton(left + (shiftX * col), 48 + (shiftY * row), this));
		}
	}
	
	private void setPhraseSet(int index)
	{
		this.currentSet = Math.abs(index)%PHRASE_SETS.length;
		updateButtons();
	}
	
	private void updateButtons()
	{
		// Send button used by compound messages
		sendButton.active = sendButton.visible = messageMode == Mode.COMPOUND;
		
		// Set buttons
		for(int i=0; i<setButtons.length; i++)
			setButtons[i].active = i != currentSet;
		
		// Phrase buttons
		PhraseSet set = PHRASE_SETS[currentSet];
		for(int i=0; i<phraseButtons.length; i++)
		{
			if(i >= set.size())
			{
				phraseButtons[i].clear();
				continue;
			}
			
			Pair<Text, Supplier<MutableText>> entry = set.get(i);
			phraseButtons[i].setPhrase(entry.getLeft(), entry.getRight());
		}
		
		setFocused(null);
	}
	
	public boolean shouldPause() { return false; }
	
	public void setMode(Mode modeIn)
	{
		if(messageMode == modeIn) return;
		messageToSend = null;
		messageMode = modeIn;
		
		updateButtons();
	}
	
	public void append(MutableText text)
	{
		switch(messageMode)
		{
			case SINGLE:
				send(text);
				break;
			case COMPOUND:
				if(messageToSend == null)
					messageToSend = text;
				else
					messageToSend.append(" ").append(text);
				break;
		}
	}
	
	public void send(@Nullable MutableText message)
	{
		if(message != null)
			AACMessagePacket.send(message);
		close();
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		context.drawText(this.textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 15, 0x404040, false);
		
		if(messageToSend != null)
		{
			ButtonWidget button = phraseButtons[phraseButtons.length - 1];
			final MutableText message = Text.translatable("aac.wheelchairs.message", mc.player.getDisplayName(), messageToSend);
			context.drawText(this.textRenderer, message, (this.width - this.textRenderer.getWidth(message)) / 2, button.getBottom() + 9, 0x404040, false);
		}
	}
	
	private class AACButton extends ButtonWidget
	{
		private final AACScreen parentScreen;
		private Supplier<MutableText> message = () -> null;
		
		protected AACButton(int x, int y, AACScreen parent)
		{
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Text.empty(), button -> 
			{
				if(!(button instanceof AACButton)) return;
				
				AACButton phrase = (AACButton)button;
				MutableText message = phrase.message.get();
				if(message != null)
					phrase.parentScreen.append(message);
				phrase.parentScreen.setFocused(null);
				
			}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			this.parentScreen = parent;
		}
		
		public void setPhrase(Text title, Supplier<MutableText> messageIn)
		{
			this.setMessage(title);
			this.message = messageIn;
		}
		
		public void clear()
		{
			setMessage(Text.empty());
			message = () -> null;
		}
	}
	
	private static class PhraseSet
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
	
	private static enum Mode
	{
		SINGLE,
		COMPOUND;
	}
	
	private static MutableText translate(String name)
	{
		return Reference.ModInfo.translate("aac", name.toLowerCase()).copy();
	}
	
	private static MutableText translate(String name, Object... args)
	{
		return Reference.ModInfo.translate("aac", name.toLowerCase(), args).copy();
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
	
	static
	{
		PHRASE_SETS = new PhraseSet[] 
				{
					new PhraseSet(translate("set_basic"), List.of(
							entry("hello"),
							entry("goodbye"),
							entry("yes"),
							entry("no"),
							entry("okay"),
							entry(translate("my_coords_title"), () -> translate("my_coords", mc.player.getBlockPos().toShortString())),
							entry(translate("my_health_title"), () -> translate("my_health", String.valueOf((int)mc.player.getHealth()))),
							entry("come_here"),
							entry("on_my_way"),
							entry("how_are_you"),
							entry("you"),
							entry("me"),
							entry("need"),
							entry("have"),
							entry("please"),
							entry("thank_you"),
							entry("more"),
							entry("less"))),
					new PhraseSet(translate("set_feelings"), List.of(
							entry("good"),
							entry("bad"),
							entry("scared"),
							entry("happy"),
							entry("fun"),
							entry("excited"),
							entry("bored"),
							entry("sad"),
							entry("tired"),
							entry("nervous"),
							entry("confused"),
							entry("understand"),
							entry("hurt"),
							entry("hungry"),
							entry("danger"),
							entry("safe"))),
					new PhraseSet(translate("set_places"), List.of(
							entry("here"),
							entry("there"),
							entry("world_spawn"),
							entry("base"),
							entry("overworld"),
							entry("the_end"),
							entry("the_nether"),
							entry("stronghold"),
							entry("dungeon"),
							entry("ocean"),
							entry("desert"),
							entry("forest"),
							entry("field"),
							entry("quarry"),
							entry("caves"),
							entry("ravine"),
							entry("village"),
							entry("farm"),
							entry("spawner"))),
					new PhraseSet(translate("set_actions"), List.of(
							entry("explore"),
							entry("dig"),
							entry("craft"),
							entry("build"),
							entry("cook"),
							entry("enchant"),
							entry("look"),
							entry("going"),
							entry("help"),
							entry("eat"),
							entry("find"),
							entry("fight"),
							entry("swim"),
							entry("sail"),
							entry("fly"),
							entry("sleep"),
							entry("follow"),
							entry("stay"),
							entry("defeat"),
							entry("lost"))),
					new PhraseSet(Text.literal("Things"), List.of(
							entry("food"),
							entry("potion"),
							entry("fish"),
							entry("water"),
							entry("lava"),
							entry("bucket"),
							entry("pickaxe"),
							entry("axe"),
							entry("shovel"),
							entry("sword"),
							entry("leather"),
							entry("iron"),
							entry("gold"),
							entry("diamond"),
							entry("armor"),
							entry("helmet"),
							entry("chestplate"),
							entry("leggings"),
							entry("boots"),
							entry("monster"),
							entry("animal"),
							entry("trader"),
							entry("pet"),
							entry("boss"),
							entry("dragon")))
				};
	}
}
