package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ButtonWidget extends AbstractButton
{
	private final List<IPressHandler> pressHandlers;
	private Component[] tooltip;

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.pressHandlers = new ArrayList<>();
		this.tooltip = new Component[0];
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
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		GuiHelper.blitButton(pPoseStack, this.x, this.y, this.width, this.height, this.active, this.isHoveredOrFocused());

		this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
		int j = getFGColor();
		GuiHelper.drawScaledText(pPoseStack, this.getMessage(), this.x + 2, this.y + this.height / 2 - 4, this.width - 4, j | Mth.ceil(this.alpha * 255.0F) << 24, true, TextAlignment.CENTER);
	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHoveredOrFocused())
		{
			GuiHelper.renderComponentTooltip(pPoseStack, pMouseX, pMouseY, this.getTooltip());
		}

	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{
		this.defaultButtonNarrationText(pNarrationElementOutput);
	}

	public void setTooltip(Component... tooltip)
	{
		this.tooltip = tooltip.clone();
	}

	public Component[] getTooltip()
	{
		return this.tooltip.clone();
	}

	public interface IPressHandler
	{
		void onPress(AbstractButton pButton);
	}

}
