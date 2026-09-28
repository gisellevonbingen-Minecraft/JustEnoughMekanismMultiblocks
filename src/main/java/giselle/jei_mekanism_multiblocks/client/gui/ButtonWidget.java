package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ButtonWidget extends ElementWidget
{
	private final List<IPressHandler> pressHandlers;

	public ButtonWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.pressHandlers = new ArrayList<>();
		this.playDownSound = true;
	}

	public void addPressHandler(IPressHandler handler)
	{
		this.pressHandlers.add(handler);
	}

	public void onPress()
	{
		for (IPressHandler handler : this.pressHandlers)
		{
			handler.onPress(this);
		}

	}

	@Override
	public void onClick(double pMouseX, double pMouseY)
	{
		this.onPress();
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
	{
		if (this.active && this.visible)
		{
			if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_SPACE || pKeyCode == GLFW.GLFW_KEY_KP_ENTER)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				this.onPress();
				return true;
			}

		}

		return false;
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
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{
		this.defaultButtonNarrationText(pNarrationElementOutput);
	}

	public interface IPressHandler
	{
		void onPress(ButtonWidget pButton);
	}

}
