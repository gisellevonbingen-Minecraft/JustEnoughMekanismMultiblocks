package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		int color = this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24;
		ITextComponent message = this.getMessage();
		float textWidth = font.width(message);
		float textX = this.x + (float) this.getAlignment().align(this.width, textWidth);
		float textY = this.y + (this.height - font.lineHeight) / 2.0F;
		font.drawShadow(pMatrixStack, message, textX, textY, color);
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
