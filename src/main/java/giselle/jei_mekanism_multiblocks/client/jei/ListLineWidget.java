package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import net.minecraft.client.gui.widget.Widget;

public class ListLineWidget extends ListWidget
{
	public boolean drawBG = false;
	public int bgColor = 0x00000000;

	public ListLineWidget(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		if (this.drawBG)
		{
			GuiHelper.fillRectagle(pMatrixStack, this.x, this.y, this.getWidth(), this.getHeight(), this.bgColor);
		}

		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	protected void onRenderWidget(List<Widget> widgets, Widget widget, MatrixStack pMatrixStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		super.onRenderWidget(widgets, widget, pMatrixStack, childMouseX, childMouseY, pPartialTicks);

		if (widgets == this.getChildren() && widget.visible)
		{
			GuiHelper.fillRectagleBlack(pMatrixStack, 0, widget.y + widget.getHeight(), this.getWidth(), 1);
		}

	}

}
