package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import net.minecraft.client.gui.components.AbstractWidget;

public class ListLineWidget extends ListWidget
{
	public ListLineWidget(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	@Override
	protected void onRenderWidget(List<AbstractWidget> widgets, AbstractWidget widget, PoseStack pPoseStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		super.onRenderWidget(widgets, widget, pPoseStack, childMouseX, childMouseY, pPartialTicks);

		if (widgets == this.getChildren() && widget.visible)
		{
			GuiHelper.fillRectagleBlack(pPoseStack, 0, widget.y + widget.getHeight(), this.getWidth(), 1);
		}

	}

}
