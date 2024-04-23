package com.lying.wheelchairs.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class ChairInventoryScreen extends AbstractInventoryScreen<ChairInventoryScreenHandler>
{
	public ChairInventoryScreen(ChairInventoryScreenHandler screenHandler, PlayerInventory playerInventory, Text text)
	{
		super(screenHandler, playerInventory, text);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		// TODO Auto-generated method stub

	}

}
