package giselle.jei_mekanism_multiblocks.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHelper
{
	public static final ResourceLocation WIDGETS_LOCATION = JEI_MekanismMultiblocks.rl("textures/gui/widgets.png");

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

	public static void drawTextScaledShadow(MatrixStack pMatrixStack, ITextComponent text, int x, int y, int width, int color)
	{
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		float scale = Math.min((float) width / (float) font.width(text), 1.0F);
		pMatrixStack.pushPose();
		pMatrixStack.scale(scale, scale, scale);
		font.drawShadow(pMatrixStack, text, x / scale, y / scale + (1.0F - scale) * font.lineHeight, color);
		pMatrixStack.popPose();
	}

	private GuiHelper()
	{

	}

}
