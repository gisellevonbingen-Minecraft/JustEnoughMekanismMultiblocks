package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
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
		this.value = pValue;
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
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		int u0 = 0;
		int u1 = u0 + 200;
		int v0 = 46 + this.getYImage(this.isHovered()) * 20;
		int v1 = v0 + 20;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		blit9Patch(pMatrixStack, this.x, this.y, this.width, this.height, u0, u1, v0, v1, 8, 8, 8, 8, 256, 256);

		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		int j = getFGColor();
		drawCenteredString(pMatrixStack, minecraft.font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
	{
		if (!this.active)
		{
			return;
		}

		pMinecraft.getTextureManager().bind(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int u0 = 0;
		int u1 = u0 + 200;
		int v0 = 46 + (this.isHovered() ? 2 : 1) * 20;
		int v1 = v0 + 20;
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

		blit9Patch(pMatrixStack, cursorX, cursorY, cursorWidth, cursorHeight, u0, u1, v0, v1, 8, 8, 8, 8, 256, 256);
	}

	private void blit9Patch(MatrixStack pMatrixStack, int x, int y, int width, int height, int textureL, int textureR, int textureT, int textureB, int uL, int vT, int uR, int vB, int textureWidth, int textureHeight)
	{
		uL = Math.min(uL, width / 2);
		uR = Math.min(uR, width / 2);
		vT = Math.min(vT, height / 2);
		vB = Math.min(vB, height / 2);

		int inL = x + uL;
		int inR = x + width - uR;
		int inT = y + vT;
		int inB = y + height - vB;
		int inW = width - uL - uR;
		int inH = height - vT - vB;

		int textureInL = textureL + uL;
		int textureInR = textureR - uR;
		int textureInT = textureT + vT;
		int textureInB = textureB - vB;
		int textureInW = textureInR - textureInL;
		int textureInH = textureInB - textureInT;

		// Left -Top
		AbstractGui.blit(pMatrixStack, x, y, uL, vT, textureL, textureT, uL, vT, textureWidth, textureHeight);
		// Right - Top
		AbstractGui.blit(pMatrixStack, inR, y, uR, vT, textureInR, textureT, uR, vT, textureWidth, textureHeight);
		// Right - Bottom
		AbstractGui.blit(pMatrixStack, x, inB, uL, vB, textureL, textureInB, uL, vB, textureWidth, textureHeight);
		// Left - Bottom
		AbstractGui.blit(pMatrixStack, inR, inB, uR, vB, textureInR, textureInB, uR, vB, textureWidth, textureHeight);

		// Top
		AbstractGui.blit(pMatrixStack, inL, y, inW, vT, textureInL, textureT, textureInW, vT, textureWidth, textureHeight);
		// Right
		AbstractGui.blit(pMatrixStack, inR, inT, uR, inH, textureInR, textureInT, uR, textureInH, textureWidth, textureHeight);
		// Bottom
		AbstractGui.blit(pMatrixStack, inL, inB, inW, vT, textureInL, textureInB, textureInW, vB, textureWidth, textureHeight);
		// Left
		AbstractGui.blit(pMatrixStack, x, inT, uL, inH, textureL, textureInT, uL, textureInH, textureWidth, textureHeight);

		// Inner
		AbstractGui.blit(pMatrixStack, inL, inT, inW, inH, textureInL, textureInT, textureInW, textureInH, textureWidth, textureHeight);
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
