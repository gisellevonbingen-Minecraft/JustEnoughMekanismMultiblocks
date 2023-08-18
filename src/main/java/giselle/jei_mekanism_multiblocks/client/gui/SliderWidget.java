package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SliderWidget extends Widget
{
	private final List<DoubleConsumer> ratioChangeHandlers;
	private double ratio;
	private boolean horizontal;
	private ITextComponent[] tooltip;

	public SliderWidget()
	{
		this(0, 0, 0, 0, StringTextComponent.EMPTY, 0);
	}

	public SliderWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, double pRatio)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.ratioChangeHandlers = new ArrayList<>();
		this.ratio = MathHelper.clamp(pRatio, 0.0D, 1.0D);
		this.horizontal = true;
		this.tooltip = new ITextComponent[0];
	}

	public void addRatioChangeHanlder(DoubleConsumer handler)
	{
		this.ratioChangeHandlers.add(handler);
	}

	@Override
	protected IFormattableTextComponent createNarrationMessage()
	{
		return new TranslationTextComponent("gui.narrate.slider", this.getMessage());
	}

	@Override
	protected int getYImage(boolean pIsHovered)
	{
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pMatrixStack, this.x, this.y, this.width, this.height, false, false);

		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		int j = this.getFGColor();
		GuiHelper.drawScaledText(pMatrixStack, this.getMessage(), this.x + 2, this.y + 1, this.width - 4, j, true, TextAlignment.CENTER);
	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHovered())
		{
			GuiHelper.renderComponentTooltip(pMatrixStack, pMouseX, pMouseY, this.getTooltip());
		}

	}

	@Override
	@SuppressWarnings("deprecation")
	protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
	{
		if (!this.active)
		{
			return;
		}

		int cursorX = 0;
		int cursorY = 0;
		int cursorWidth = 0;
		int cursorHeight = 0;

		if (this.isHorizontal())
		{
			cursorX = this.x + (int) (this.ratio * (this.width - 8));
			cursorY = this.y;
			cursorWidth = 8;
			cursorHeight = this.height;
		}
		else
		{
			cursorX = this.x;
			cursorY = this.y + (int) (this.ratio * (this.height - 8));
			cursorWidth = this.width;
			cursorHeight = 8;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pMatrixStack, cursorX, cursorY, cursorWidth, cursorHeight, true, this.isHovered());
	}

	protected void setRatioFromMouse(double pMouseX, double pMouseY)
	{
		double ratio = 0.0D;

		if (this.isHorizontal())
		{
			ratio = (pMouseX - (this.x + 4)) / (this.width - 8);
		}
		else
		{
			ratio = (pMouseY - (this.y + 4)) / (this.height - 8);
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
		pRatio = MathHelper.clamp(pRatio, 0.0D, 1.0D);

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
	public void playDownSound(SoundHandler pHandler)
	{

	}

	@Override
	public void onRelease(double pMouseX, double pMouseY)
	{
		super.onRelease(pMouseX, pMouseY);

		super.playDownSound(Minecraft.getInstance().getSoundManager());
	}

	public void setTooltip(ITextComponent... tooltip)
	{
		this.tooltip = tooltip.clone();
	}

	public ITextComponent[] getTooltip()
	{
		return tooltip.clone();
	}

}
