package giselle.jei_mekanism_multiblocks.client.gui;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LabelWidget extends AbstractWidget
{
	private boolean shadow;
	private TextAlignment alignment;

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		this(pX, pY, pWidth, pHeight, pMessage, TextAlignment.CENTER);
	}

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, TextAlignment alignment)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.shadow = true;
		this.alignment = alignment;
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int color = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
		Component message = this.getMessage();
		GuiHelper.drawScaledText(pGuiGraphics, message, this.getX(), this.getY(), this.width, color, this.isShadow(), this.getAlignment());
	}

	@Override
	public void playDownSound(SoundManager pHandler)
	{

	}

	public boolean isShadow()
	{
		return this.shadow;
	}

	public void setShadow(boolean shadow)
	{
		this.shadow = shadow;
	}

	public TextAlignment getAlignment()
	{
		return this.alignment;
	}

	public void setAlignment(TextAlignment alignment)
	{
		if (alignment == null)
		{
			alignment = TextAlignment.CENTER;
		}

		this.alignment = alignment;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
