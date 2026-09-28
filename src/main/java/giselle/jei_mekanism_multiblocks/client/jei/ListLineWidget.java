package giselle.jei_mekanism_multiblocks.client.jei;

import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
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
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		if (this.drawBG)
		{
			GuiHelper.fillRectagle(pPoseStack, this.x, this.y, this.getWidth(), this.getHeight(), this.bgColor);
		}

		super.render(pPoseStack, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	protected void onRenderItemBackground(AbstractWidget widget, PoseStack pPoseStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		super.onRenderItemBackground(widget, pPoseStack, childMouseX, childMouseY, pPartialTicks);

		if (widget.visible)
		{
			GuiHelper.fillRectagleBlack(pPoseStack, -this.getItemsLeft(), widget.y + widget.getHeight(), this.getWidth(), 1);
		}

	}

}
