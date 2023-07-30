package giselle.jei_mekanism_multiblocks.client;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHelper
{
	public static final ResourceLocation WIDGETS_LOCATION = JEI_MekanismMultiblocks.rl("textures/gui/widgets.png");

	public static void renderComponentTooltip(MatrixStack pMatrixStack, int pMouseX, int pMouseY, ITextComponent... tooltip)
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && tooltip.length > 0)
		{
			minecraft.screen.renderComponentTooltip(pMatrixStack, Arrays.asList(tooltip), pMouseX, pMouseY);
		}

	}

	public static void renderComponentTooltip(MatrixStack pMatrixStack, int pMouseX, int pMouseY, List<ITextComponent> tooltip)
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && tooltip.size() > 0)
		{
			minecraft.screen.renderComponentTooltip(pMatrixStack, tooltip, pMouseX, pMouseY);
		}

	}

	public static void fillRectagleBlack(MatrixStack pMatrixStack, int x, int y, int width, int height)
	{
		fillRectagle(pMatrixStack, x, y, width, height, 0.0F, 0.0F, 0.0F, 1.0F);
	}

	@SuppressWarnings("deprecation")
	public static void fillRectagle(MatrixStack pMatrixStack, int x, int y, int width, int height, float r, float g, float b, float a)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);
		RenderSystem.color4f(r, g, b, a);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		AbstractGui.blit(pMatrixStack, x, y, width, height, 0, 0, 16, 16, 256, 256);
	}

	public static void drawScaledText(MatrixStack pMatrixStack, ITextComponent text, int x, int y, int width, int color, boolean shadow)
	{
		drawScaledText(pMatrixStack, text, x, y, width, color, shadow, TextAlignment.LEFT);
	}

	public static void drawScaledText(MatrixStack pMatrixStack, ITextComponent text, int x, int y, int width, int color, boolean shadow, TextAlignment alignment)
	{
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		int textWidth = font.width(text);
		float scale = Math.min((float) width / (float) textWidth, 1.0F);
		pMatrixStack.pushPose();
		pMatrixStack.scale(scale, scale, 1.0F);

		float scaledX = (x + (float) alignment.align(width, textWidth * scale)) / scale;
		float scaledY = y / scale + (1.0F - scale) * font.lineHeight;

		if (shadow)
		{
			font.drawShadow(pMatrixStack, text, scaledX, scaledY, color);
		}
		else
		{
			font.draw(pMatrixStack, text, scaledX, scaledY, color);
		}

		pMatrixStack.popPose();
	}

	public static void blitButton(MatrixStack pMatrixStack, int x, int y, int width, int height, boolean active, boolean hovered)
	{
		int i = active ? hovered ? 2 : 1 : 0;
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);
		GuiHelper.blit9Patch(pMatrixStack, x, y, width, height, i * 20, 52, 20, 20, 2, 2, 2, 2);
	}

	public static void blit9Patch(MatrixStack pMatrixStack, int x, int y, int width, int height, int textureX, int textureY, int textureW, int textureH, int uL, int vT, int uR, int vB)
	{
		blit9Patch(pMatrixStack, x, y, width, height, textureX, textureY, textureW, textureH, uL, vT, uR, vB, 256, 256);
	}

	public static void blit9Patch(MatrixStack pMatrixStack, int x, int y, int width, int height, int textureX, int textureY, int textureW, int textureH, int uL, int vT, int uR, int vB, int textureWidth, int textureHeight)
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
		AbstractGui.blit(pMatrixStack, x, y, uL, vT, textureX, textureY, uL, vT, textureWidth, textureHeight);
		// Right - Top
		AbstractGui.blit(pMatrixStack, inR, y, uR, vT, textureInR, textureY, uR, vT, textureWidth, textureHeight);
		// Right - Bottom
		AbstractGui.blit(pMatrixStack, x, inB, uL, vB, textureX, textureInB, uL, vB, textureWidth, textureHeight);
		// Left - Bottom
		AbstractGui.blit(pMatrixStack, inR, inB, uR, vB, textureInR, textureInB, uR, vB, textureWidth, textureHeight);

		// Top
		AbstractGui.blit(pMatrixStack, inL, y, inW, vT, textureInL, textureY, textureInW, vT, textureWidth, textureHeight);
		// Right
		AbstractGui.blit(pMatrixStack, inR, inT, uR, inH, textureInR, textureInT, uR, textureInH, textureWidth, textureHeight);
		// Bottom
		AbstractGui.blit(pMatrixStack, inL, inB, inW, vT, textureInL, textureInB, textureInW, vB, textureWidth, textureHeight);
		// Left
		AbstractGui.blit(pMatrixStack, x, inT, uL, inH, textureX, textureInT, uL, textureInH, textureWidth, textureHeight);

		// Inner
		AbstractGui.blit(pMatrixStack, inL, inT, inW, inH, textureInL, textureInT, textureInW, textureInH, textureWidth, textureHeight);
	}

	private GuiHelper()
	{

	}

}
