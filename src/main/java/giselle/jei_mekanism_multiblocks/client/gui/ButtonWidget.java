package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class ButtonWidget extends AbstractButton
{
	public static final ITooltip NO_TOOLTIP = (pButton, pMatrixStack, pMouseX, pMouseY) ->
	{

	};

	private final IPressable onPress;
	private final ITooltip onTooltip;

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress)
	{
		this(pX, pY, pWidth, pHeight, pMessage, pOnPress, NO_TOOLTIP);
	}

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress, ITooltip pOnTooltip)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.onPress = pOnPress;
		this.onTooltip = pOnTooltip != null ? pOnTooltip : null;
	}

	@Override
	public void onPress()
	{
		this.onPress.onPress(this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pMatrixStack, this.x, this.y, this.width, this.height, this.active, this.isHovered());

		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		int j = getFGColor();
		AbstractGui.drawCenteredString(pMatrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

		if (this.isHovered())
		{
			this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
		}

	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		this.onTooltip.onTooltip(this, pMatrixStack, pMouseX, pMouseY);
	}

	public interface IPressable
	{
		void onPress(AbstractButton pButton);
	}

	public interface ITooltip
	{
		void onTooltip(AbstractButton pButton, MatrixStack pMatrixStack, int pMouseX, int pMouseY);
	}

}
