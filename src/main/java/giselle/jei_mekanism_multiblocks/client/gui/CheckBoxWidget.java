package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class CheckBoxWidget extends AbstractButton
{
	private final List<Consumer<Boolean>> selectedChangedHandlers;
	private Component[] tooltip;
	private boolean selected;

	public CheckBoxWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.selectedChangedHandlers = new ArrayList<>();
		this.tooltip = new Component[0];
		this.selected = pSelected;
	}

	public void addSelectedChangedHandler(Consumer<Boolean> handler)
	{
		this.selectedChangedHandlers.add(handler);
	}

	@Override
	public void onPress()
	{
		this.setSelected(!this.selected);
	}

	public boolean isSelected()
	{
		return this.selected;
	}

	public void setSelected(boolean selected)
	{
		if (this.isSelected() != selected)
		{
			this.selected = selected;

			for (Consumer<Boolean> handler : this.selectedChangedHandlers)
			{
				handler.accept(selected);
			}

		}

	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, GuiHelper.WIDGETS_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int checkerLength = this.height;
		GuiComponent.blit(pPoseStack, this.x, this.y, checkerLength, checkerLength, 0.0F + (this.isHoveredOrFocused() ? 10.0F : 0.0F), 16.0F + (this.selected ? 10.0F : 0.0F), 10, 10, 256, 256);
		this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);

		GuiHelper.drawScaledText(pPoseStack, this.getMessage(), this.x + checkerLength + 1, this.y, this.width - checkerLength - 1, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24, true);

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

}
