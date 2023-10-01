package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class SliderWidget extends AbstractWidget
{
	private final List<DoubleConsumer> ratioChangeHandlers;
	private double ratio;
	private boolean horizontal;

	public SliderWidget()
	{
		this(0, 0, 0, 0, Component.empty(), 0);
	}

	public SliderWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, double pRatio)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.ratioChangeHandlers = new ArrayList<>();
		this.ratio = Mth.clamp(pRatio, 0.0D, 1.0D);
		this.horizontal = true;
	}

	public void addRatioChangeHanlder(DoubleConsumer handler)
	{
		this.ratioChangeHandlers.add(handler);
	}

	@Override
	protected MutableComponent createNarrationMessage()
	{
		return Component.translatable("gui.narrate.slider", this.getMessage());
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pGuiGraphics, this.getX(), this.getY(), this.width, this.height, false, false);

		if (this.active)
		{
			int cursorX = 0;
			int cursorY = 0;
			int cursorWidth = 0;
			int cursorHeight = 0;

			if (this.isHorizontal())
			{
				cursorX = this.getX() + (int) (this.ratio * (this.width - 8));
				cursorY = this.getY();
				cursorWidth = 8;
				cursorHeight = this.height;
			}
			else
			{
				cursorX = this.getX();
				cursorY = this.getY() + (int) (this.ratio * (this.height - 8));
				cursorWidth = this.width;
				cursorHeight = 8;
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			GuiHelper.blitButton(pGuiGraphics, cursorX, cursorY, cursorWidth, cursorHeight, true, this.isHoveredOrFocused());

		}

		int j = this.getFGColor();
		GuiHelper.drawScaledText(pGuiGraphics, this.getMessage(), this.getX() + 2, this.getY() + 1, this.width - 4, j, true, TextAlignment.CENTER);
	}

	protected void setRatioFromMouse(double pMouseX, double pMouseY)
	{
		double ratio = 0.0D;

		if (this.isHorizontal())
		{
			ratio = (pMouseX - (this.getX() + 4)) / (this.width - 8);
		}
		else
		{
			ratio = (pMouseY - (this.getY() + 4)) / (this.height - 8);
		}

		double appliedRatio = this.applyRatioFromMouse(ratio);
		this.setRatio(appliedRatio);
	}

	protected double applyRatioFromMouse(double pRatio)
	{
		return pRatio;
	}

	@Override
	public void onClick(double pMouseX, double pMouseY)
	{
		this.setRatioFromMouse(pMouseX, pMouseY);
	}

	@Override
	protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY)
	{
		this.setRatioFromMouse(pMouseX, pMouseY);
		super.onDrag(pMouseX, pMouseY, pDragX, pDragY);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
	{
		boolean flag = pKeyCode == 263;

		if (flag || pKeyCode == 262)
		{
			float f = flag ? -1.0F : 1.0F;
			this.setRatio(this.ratio + f / (this.width - 8));
		}

		return false;
	}

	public double getRatio()
	{
		return this.ratio;
	}

	public void setRatio(double pRatio)
	{
		pRatio = Mth.clamp(pRatio, 0.0D, 1.0D);

		if (this.getRatio() != pRatio)
		{
			this.ratio = pRatio;
			this.onRatioChanged();
		}

	}

	protected void onRatioChanged()
	{
		double ratio = this.getRatio();

		for (DoubleConsumer handler : this.ratioChangeHandlers)
		{
			handler.accept(ratio);
		}

	}

	public boolean isHorizontal()
	{
		return this.horizontal;
	}

	public boolean isVertical()
	{
		return !this.isHorizontal();
	}

	public void setHorizontal(boolean horizontal)
	{
		this.horizontal = horizontal;
	}

	public void setHorizontal()
	{
		this.setHorizontal(true);
	}

	public void setVertical()
	{
		this.setHorizontal(false);
	}

	@Override
	public void playDownSound(SoundManager pHandler)
	{

	}

	@Override
	public void onRelease(double pMouseX, double pMouseY)
	{
		super.onRelease(pMouseX, pMouseY);

		super.playDownSound(Minecraft.getInstance().getSoundManager());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{
		pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());

		if (this.active)
		{
			if (this.isFocused())
			{
				pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
			}
			else
			{
				pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
			}

		}

	}

}
