package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class ButtonWidget extends AbstractButton
{
	private final List<IPressHandler> pressHandlers;
	private ITextComponent[] tooltip;

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.pressHandlers = new ArrayList<>();
		this.tooltip = new ITextComponent[0];
	}

	public void addPressHandler(IPressHandler handler)
	{
		this.pressHandlers.add(handler);
	}

	@Override
	public void onPress()
	{
		for (IPressHandler handler : this.pressHandlers)
		{
			handler.onPress(this);
		}

	}

	@Override
	@SuppressWarnings("deprecation")
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pMatrixStack, this.x, this.y, this.width, this.height, this.active, this.isHovered());

		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		int j = getFGColor();
		GuiHelper.drawScaledText(pMatrixStack, this.getMessage(), this.x + 2, this.y + this.height / 2 - 4, this.width - 4, j | MathHelper.ceil(this.alpha * 255.0F) << 24, true, TextAlignment.CENTER);
	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHovered())
		{
			GuiHelper.renderComponentTooltip(pMatrixStack, pMouseX, pMouseY, this.getTooltip());
		}

	}

	public void setTooltip(ITextComponent... tooltip)
	{
		this.tooltip = tooltip.clone();
	}

	public ITextComponent[] getTooltip()
	{
		return this.tooltip.clone();
	}

	public interface IPressHandler
	{
		void onPress(AbstractButton pButton);
	}

}
