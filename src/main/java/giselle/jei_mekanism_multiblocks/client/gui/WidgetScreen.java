package giselle.jei_mekanism_multiblocks.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WidgetScreen extends Screen
{
	protected WidgetScreen(Component pTitle)
	{
		super(pTitle);
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
	{
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

		this.renderForeground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

		this.renderTooltip(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	protected void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
	{

	}

	protected void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
	{
		for (GuiEventListener widget : this.children())
		{
			if (widget instanceof ElementWidget)
			{
				((ElementWidget) widget).renderToolTip(pGuiGraphics, pMouseX, pMouseY);
			}

		}

	}

}
