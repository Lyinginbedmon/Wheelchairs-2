package com.lying.wheelchairs.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WalkerInventoryScreen extends AbstractInventoryScreen<WalkerInventoryScreenHandler>
{
	private static final Identifier TEXTURE = ChairInventoryScreen.TEXTURE;
	
	public WalkerInventoryScreen(WalkerInventoryScreenHandler screenHandler, PlayerInventory playerInventory, Text text)
	{
		super(screenHandler, playerInventory, text);
		this.playerInventoryTitleY = this.backgroundHeight - 92;
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int i = (this.width - 174) / 2;
		int j = (this.height - 164) / 2;
		context.drawTexture(TEXTURE, i, j, 0, 0, 174, 164);
		context.drawTexture(TEXTURE, this.width / 2 - 45, this.height / 2 - 66, 0, 164, 90, 54);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawText(this.textRenderer, this.title, (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2, this.titleY, 0x404040, false);
		context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
	}
}
