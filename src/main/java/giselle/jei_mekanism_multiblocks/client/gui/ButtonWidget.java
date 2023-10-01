package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ButtonWidget extends AbstractButton
{
	private final List<IPressHandler> pressHandlers;

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.pressHandlers = new ArrayList<>();
	}

	public void addPressHandler(IPressHandler handler)
	{
		this.pressHandlers.add(handler);
	}

	@Override
	public void onPress()
	{
		for (IPressHandler handler : this.pressHandlers)
		{
			handler.onPress(this);
		}

	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pGuiGraphics, this.getX(), this.getY(), this.width, this.height, this.active, this.isHoveredOrFocused());

		int j = getFGColor();
		GuiHelper.drawScaledText(pGuiGraphics, this.getMessage(), this.getX(), this.getY() + (this.height - 8) / 2, this.width, j | Mth.ceil(this.alpha * 255.0F) << 24, true, TextAlignment.CENTER);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{
		this.defaultButtonNarrationText(pNarrationElementOutput);
	}

	public interface IPressHandler
	{
		void onPress(AbstractButton pButton);
	}

}
