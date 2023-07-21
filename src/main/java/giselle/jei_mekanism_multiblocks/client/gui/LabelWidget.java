package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class LabelWidget extends Widget
{
	private TextAlignment alignment;

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		this(pX, pY, pWidth, pHeight, pMessage, TextAlignment.CENTER);
	}

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, TextAlignment alignment)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.alignment = alignment;
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int color = this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24;
		ITextComponent message = this.getMessage();
		GuiHelper.drawTextScaledShadow(pMatrixStack, message, this.x, this.y, this.width, color, this.alignment);
	}

	public TextAlignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(TextAlignment alignment)
	{
		if (alignment == null)
		{
			alignment = TextAlignment.CENTER;
		}

		this.alignment = alignment;
	}

}
