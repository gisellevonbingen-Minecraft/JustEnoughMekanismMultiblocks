package giselle.jei_mekanism_multiblocks.client.jei;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

public class ListLineWidget extends ListWidget
{
	public boolean drawBG = false;
	public int bgColor = 0x00000000;

	public ListLineWidget(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		if (this.drawBG)
		{
			GuiHelper.fillRectagle(pGuiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.bgColor);
		}

		super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	protected void onRenderItemBackground(AbstractWidget widget, GuiGraphics pGuiGraphics, int childMouseX, int childMouseY, float pPartialTicks)
	{
		super.onRenderItemBackground(widget, pGuiGraphics, childMouseX, childMouseY, pPartialTicks);

		if (widget.visible)
		{
			GuiHelper.fillRectagleBlack(pGuiGraphics, -this.getItemsLeft(), widget.getY() + widget.getHeight(), this.getWidth(), 1);
		}

	}

}
