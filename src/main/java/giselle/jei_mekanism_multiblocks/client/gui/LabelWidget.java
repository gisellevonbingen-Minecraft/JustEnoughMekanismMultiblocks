package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class LabelWidget extends Widget
{
	public enum Alignment
	{
		Left,
		Center,
		Right,
	}

	private Alignment alignment;

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		this(pX, pY, pWidth, pHeight, pMessage, Alignment.Center);
	}

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Alignment alignment)
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
		Alignment alignment = this.getAlignment();

		float textX = 0.0F;
		float textWidth = font.width(message);

		if (alignment == Alignment.Left)
		{
			textX = this.x;
		}
		else if (alignment == Alignment.Center)
		{
			textX = this.x + (this.width - textWidth) / 2.0F;
		}
		else if (alignment == Alignment.Right)
		{
			textX = this.x + (this.width - textWidth);
		}

		float textY = this.y + (this.height - font.lineHeight) / 2.0F;
		font.drawShadow(pMatrixStack, message, textX, textY, color);
	}

	public Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(Alignment alignment)
	{
		if (alignment == null)
		{
			alignment = Alignment.Center;
		}

		this.alignment = alignment;
	}

}
