package giselle.jei_mekanism_multiblocks.client;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiHelper
{
	public static final ResourceLocation WIDGETS_LOCATION = JEI_MekanismMultiblocks.rl("textures/gui/widgets.png");

	public static void renderComponentTooltip(PoseStack pPoseStack, int pMouseX, int pMouseY, Component... tooltip)
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && tooltip.length > 0)
		{
			minecraft.screen.renderComponentTooltip(pPoseStack, Arrays.asList(tooltip), pMouseX, pMouseY);
		}

	}

	public static void renderComponentTooltip(PoseStack pPoseStack, int pMouseX, int pMouseY, List<Component> tooltip)
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && tooltip.size() > 0)
		{
			minecraft.screen.renderComponentTooltip(pPoseStack, tooltip, pMouseX, pMouseY);
		}

	}

	public static void fillRectagleBlack(PoseStack pPoseStack, int x, int y, int width, int height)
	{
		RenderSystem.enableDepthTest();
		GuiComponent.fill(pPoseStack, x, y, x + width, y + height, 0xFF000000);
	}

	public static void fillRectagle(PoseStack pPoseStack, int x, int y, int width, int height, float r, float g, float b, float a)
	{
		int color = Mth.color(r, g, b) | (Mth.floor(255.0F * a) << 0x18);
		RenderSystem.enableDepthTest();
		GuiComponent.fill(pPoseStack, x, y, x + width, y + height, color);
	}

	public static void drawScaledText(PoseStack pPoseStack, Component text, int x, int y, int width, int color, boolean shadow)
	{
		drawScaledText(pPoseStack, text, x, y, width, color, shadow, TextAlignment.LEFT);
	}

	public static void drawScaledText(PoseStack pPoseStack, Component text, int x, int y, int width, int color, boolean shadow, TextAlignment alignment)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int textWidth = font.width(text);
		float scale = Math.min((float) width / (float) textWidth, 1.0F);
		pPoseStack.pushPose();
		pPoseStack.scale(scale, scale, 1.0F);

		float scaledX = (x + (float) alignment.align(width, textWidth * scale)) / scale;
		float scaledY = y / scale + (1.0F - scale) * font.lineHeight;

		if (shadow)
		{
			font.drawShadow(pPoseStack, text, scaledX, scaledY, color);
		}
		else
		{
			font.draw(pPoseStack, text, scaledX, scaledY, color);
		}

		pPoseStack.popPose();
	}

	public static void blitButton(PoseStack pPoseStack, int x, int y, int width, int height, boolean active, boolean hovered)
	{
		int i = active ? hovered ? 2 : 1 : 0;
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		GuiHelper.blit9Patch(pPoseStack, x, y, width, height, i * 20, 52, 20, 20, 2, 2, 2, 2);
	}

	public static void blit9Patch(PoseStack pPoseStack, int x, int y, int width, int height, int textureX, int textureY, int textureW, int textureH, int uL, int vT, int uR, int vB)
	{
		blit9Patch(pPoseStack, x, y, width, height, textureX, textureY, textureW, textureH, uL, vT, uR, vB, 256, 256);
	}

	public static void blit9Patch(PoseStack pPoseStack, int x, int y, int width, int height, int textureX, int textureY, int textureW, int textureH, int uL, int vT, int uR, int vB, int textureWidth, int textureHeight)
	{
		uL = Math.min(uL, width / 2);
		uR = Math.min(uR, width / 2);
		vT = Math.min(vT, height / 2);
		vB = Math.min(vB, height / 2);

		int inL = x + uL;
		int inR = x + width - uR;
		int inT = y + vT;
		int inB = y + height - vB;
		int inW = width - uL - uR;
		int inH = height - vT - vB;

		int textureInL = textureX + uL;
		int textureInR = textureX + textureW - uR;
		int textureInT = textureY + vT;
		int textureInB = textureY + textureH - vB;
		int textureInW = textureInR - textureInL;
		int textureInH = textureInB - textureInT;

		// Left -Top
		GuiComponent.blit(pPoseStack, x, y, uL, vT, textureX, textureY, uL, vT, textureWidth, textureHeight);
		// Right - Top
		GuiComponent.blit(pPoseStack, inR, y, uR, vT, textureInR, textureY, uR, vT, textureWidth, textureHeight);
		// Right - Bottom
		GuiComponent.blit(pPoseStack, x, inB, uL, vB, textureX, textureInB, uL, vB, textureWidth, textureHeight);
		// Left - Bottom
		GuiComponent.blit(pPoseStack, inR, inB, uR, vB, textureInR, textureInB, uR, vB, textureWidth, textureHeight);

		// Top
		GuiComponent.blit(pPoseStack, inL, y, inW, vT, textureInL, textureY, textureInW, vT, textureWidth, textureHeight);
		// Right
		GuiComponent.blit(pPoseStack, inR, inT, uR, inH, textureInR, textureInT, uR, textureInH, textureWidth, textureHeight);
		// Bottom
		GuiComponent.blit(pPoseStack, inL, inB, inW, vT, textureInL, textureInB, textureInW, vB, textureWidth, textureHeight);
		// Left
		GuiComponent.blit(pPoseStack, x, inT, uL, inH, textureX, textureInT, uL, textureInH, textureWidth, textureHeight);

		// Inner
		GuiComponent.blit(pPoseStack, inL, inT, inW, inH, textureInL, textureInT, textureInW, textureInH, textureWidth, textureHeight);
	}

	private GuiHelper()
	{

	}

}
