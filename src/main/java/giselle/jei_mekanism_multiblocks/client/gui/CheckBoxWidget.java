package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class CheckBoxWidget extends AbstractButton
{
	private final List<Consumer<Boolean>> selectedChangedHandlers;
	private boolean selected;
	private boolean shadow;

	public CheckBoxWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.selectedChangedHandlers = new ArrayList<>();
		this.selected = pSelected;
		this.setFGColor(0x404040);
		this.shadow = false;
	}

	public void addSelectedChangedHandler(Consumer<Boolean> handler)
	{
		this.selectedChangedHandlers.add(handler);
	}

	@Override
	public void onPress()
	{
		this.setSelected(!this.isSelected());
	}

	public boolean isSelected()
	{
		return this.selected;
	}

	public void setSelected(boolean selected)
	{
		if (this.isSelected() != selected)
		{
			this.selected = selected;

			for (Consumer<Boolean> handler : this.selectedChangedHandlers)
			{
				handler.accept(selected);
			}

		}

	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int checkerLength = this.height;
		pGuiGraphics.blit(GuiHelper.WIDGETS_LOCATION, this.getX(), this.getY(), checkerLength, checkerLength, 0.0F + (this.isHoveredOrFocused() ? 10.0F : 0.0F), 16.0F + (this.selected ? 10.0F : 0.0F), 10, 10, 256, 256);

		int j = getFGColor();
		GuiHelper.drawScaledText(pGuiGraphics, this.getMessage(), this.getX() + checkerLength + 1, this.getY(), this.width - checkerLength - 1, j | Mth.ceil(this.alpha * 255.0F) << 24, this.isShadow());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{
		this.defaultButtonNarrationText(pNarrationElementOutput);

	}

	public boolean isShadow()
	{
		return this.shadow;
	}

	public void setShadow(boolean shadow)
	{
		this.shadow = shadow;
	}

}
