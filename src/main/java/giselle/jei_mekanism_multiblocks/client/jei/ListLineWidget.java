package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.List;

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
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		if (this.drawBG)
		{
			GuiHelper.fillRectagle(pGuiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.bgColor);
		}

		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	protected void onRenderWidget(List<AbstractWidget> widgets, AbstractWidget widget, GuiGraphics pGuiGraphics, int childMouseX, int childMouseY, float pPartialTicks)
	{
		super.onRenderWidget(widgets, widget, pGuiGraphics, childMouseX, childMouseY, pPartialTicks);

		if (widgets == this.getChildren() && widget.visible)
		{
			GuiHelper.fillRectagleBlack(pGuiGraphics, 0, widget.getY() + widget.getHeight(), this.getWidth(), 1);
		}

	}

}
