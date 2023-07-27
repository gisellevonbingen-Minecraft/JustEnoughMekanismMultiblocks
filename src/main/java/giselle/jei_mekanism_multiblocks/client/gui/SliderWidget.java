package giselle.jei_mekanism_multiblocks.client.gui;

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
	private double value;
	private boolean horizontal;
	private DoubleConsumer setter;

	public SliderWidget()
	{
		this(0, 0, 0, 0, StringTextComponent.EMPTY, 0);
	}

	public SliderWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, double pValue)
	{
		this(pX, pY, pWidth, pHeight, pMessage, pValue, null);
	}

	public SliderWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, double pValue, DoubleConsumer setter)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.value = MathHelper.clamp(pValue, 0.0D, 1.0D);
		this.horizontal = true;
		this.setter = setter;
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
		GuiHelper.drawTextScaledShadow(pMatrixStack, this.getMessage(), this.x + 2, this.y + 1, this.width - 4, j, TextAlignment.CENTER);
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
			cursorX = this.x + (int) (this.value * (this.width - 8));
			cursorY = this.y;
			cursorWidth = 8;
			cursorHeight = this.height;
		}
		else
		{
			cursorX = this.x;
			cursorY = this.y + (int) (this.value * (this.height - 8));
			cursorWidth = this.width;
			cursorHeight = 8;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pMatrixStack, cursorX, cursorY, cursorWidth, cursorHeight, true, this.isHovered());
	}

	protected void setValueFromMouse(double pMouseX, double pMouseY)
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

		double value = this.applyValueFromMouse(ratio);
		this.setValue(value);
	}

	protected double applyValueFromMouse(double pValue)
	{
		return pValue;
	}

	@Override
	public void onClick(double pMouseX, double pMouseY)
	{
		this.setValueFromMouse(pMouseX, pMouseY);
	}

	@Override
	protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY)
	{
		this.setValueFromMouse(pMouseX, pMouseY);
		super.onDrag(pMouseX, pMouseY, pDragX, pDragY);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
	{
		boolean flag = pKeyCode == 263;
		if (flag || pKeyCode == 262)
		{
			float f = flag ? -1.0F : 1.0F;
			this.setValue(this.value + f / (this.width - 8));
		}

		return false;
	}

	public double getValue()
	{
		return this.value;
	}

	protected void setValueInternal(double value)
	{
		this.value = MathHelper.clamp(value, 0.0D, 1.0D);
	}

	public void setValue(double value)
	{
		value = MathHelper.clamp(value, 0.0D, 1.0D);

		if (this.getValue() != value)
		{
			this.value = value;
			this.onValueChanged();
		}

	}

	protected void onValueChanged()
	{
		if (this.setter != null)
		{
			this.setter.accept(this.getValue());
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
		super.playDownSound(Minecraft.getInstance().getSoundManager());
	}

}
