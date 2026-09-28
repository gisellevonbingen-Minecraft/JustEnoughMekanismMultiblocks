package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

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
	protected void init()
	{
		super.init();
	}

	@Override
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		this.renderBackground(pPoseStack);

		super.render(pPoseStack, pMouseX, pMouseY, pPartialTicks);

		this.renderForeground(pPoseStack, pMouseX, pMouseY, pPartialTicks);

		this.renderTooltip(pPoseStack, pMouseX, pMouseY, pPartialTicks);
	}

	protected void renderForeground(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	protected void renderTooltip(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		for (GuiEventListener widget : this.children())
		{
			if (widget instanceof ElementWidget)
			{
				((ElementWidget) widget).renderToolTip(pPoseStack, pMouseX, pMouseY);
			}

		}

	}

}
