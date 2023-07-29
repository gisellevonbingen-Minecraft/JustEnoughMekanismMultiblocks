package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class LabelWidget extends Widget
{
	private boolean shadow;
	private TextAlignment alignment;
	private ITextComponent[] tooltips;

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		this(pX, pY, pWidth, pHeight, pMessage, TextAlignment.CENTER);
	}

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, TextAlignment alignment)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.shadow = true;
		this.alignment = alignment;
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int color = this.getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24;
		ITextComponent message = this.getMessage();
		GuiHelper.drawScaledText(pMatrixStack, message, this.x, this.y, this.width, color, this.isShadow(), this.getAlignment());

		if (this.isHovered())
		{
			this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
		}

	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		ITextComponent[] tooltips = this.getTooltips();

		if (tooltips != null && tooltips.length > 0)
		{
			Minecraft minecraft = Minecraft.getInstance();
			GuiUtils.drawHoveringText(pMatrixStack, Arrays.asList(tooltips), pMouseX, pMouseY, minecraft.screen.width, minecraft.screen.height, -1, minecraft.font);

		}

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

	public ITextComponent[] getTooltips()
	{
		return this.tooltips;
	}

	public void setTooltips(ITextComponent[] tooltips)
	{
		this.tooltips = tooltips;
	}

}
