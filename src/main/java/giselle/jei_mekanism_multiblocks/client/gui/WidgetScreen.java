package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class WidgetScreen extends Screen
{
	protected WidgetScreen(ITextComponent pTitle)
	{
		super(pTitle);
	}

	@Override
	protected void init()
	{
		super.init();
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		this.renderBackground(pMatrixStack);

		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		this.renderForeground(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		this.renderTooltip(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	protected void renderForeground(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	protected void renderTooltip(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		for (IGuiEventListener widget : this.children())
		{
			if (widget instanceof ElementWidget)
			{
				((ElementWidget) widget).renderToolTip(pMatrixStack, pMouseX, pMouseY);
			}

		}

	}

}
