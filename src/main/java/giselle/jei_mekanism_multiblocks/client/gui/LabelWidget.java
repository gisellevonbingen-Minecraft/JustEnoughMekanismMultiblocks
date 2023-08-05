package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LabelWidget extends AbstractWidget
{
	private boolean shadow;
	private TextAlignment alignment;
	private Component[] tooltip;

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		this(pX, pY, pWidth, pHeight, pMessage, TextAlignment.CENTER);
	}

	public LabelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, TextAlignment alignment)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.shadow = true;
		this.alignment = alignment;
		this.tooltip = new Component[0];
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int color = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
		Component message = this.getMessage();
		GuiHelper.drawScaledText(pPoseStack, message, this.x, this.y, this.width, color, this.isShadow(), this.getAlignment());

		if (this.isHoveredOrFocused())
		{
			this.renderToolTip(pPoseStack, pMouseX, pMouseY);
		}

	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
	{
		GuiHelper.renderComponentTooltip(pPoseStack, pMouseX, pMouseY, this.getTooltip());
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

	public Component[] getTooltip()
	{
		return this.tooltip.clone();
	}

	public void setTooltip(Component... tooltip)
	{
		this.tooltip = tooltip.clone();
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
