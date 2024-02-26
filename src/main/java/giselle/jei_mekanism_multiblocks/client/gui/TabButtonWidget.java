package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TabButtonWidget extends ButtonWidget
{
	private boolean selected;

	public TabButtonWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.selected = false;
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();

		int i = this.isSelected() ? 1 : 0;
		GuiHelper.blit9Patch(pGuiGraphics, GuiHelper.WIDGETS_LOCATION, this.getX(), this.getY(), this.width, this.height, 0 + i * 16, 36, 16, 16, 4, 4, 4, 4, 256, 256);

		int j = this.getFGColor();
		Component message = this.getMessage();
		GuiHelper.drawScaledText(pGuiGraphics, message, this.getX() + 3, this.getY() + (this.height - 8) / 2 + 1, this.width - 6, j | Mth.ceil(this.alpha * 255.0F) << 24, !this.selected, TextAlignment.CENTER);
	}

	public boolean isSelected()
	{
		return this.selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
		this.setFGColor(selected ? 0x404040 : 0xFFFFFF);
	}

}
