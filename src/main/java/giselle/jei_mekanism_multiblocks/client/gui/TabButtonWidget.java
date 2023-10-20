package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class TabButtonWidget extends ButtonWidget
{
	private boolean selected;

	public TabButtonWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.selected = false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(GuiHelper.WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();

		int i = this.isSelected() ? 1 : 0;
		GuiHelper.blit9Patch(pMatrixStack, this.x, this.y, this.width, this.height, 0 + i * 16, 36, 16, 16, 4, 4, 4, 4, 256, 256);
		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);

		int j = this.getFGColor();
		ITextComponent message = this.getMessage();
		GuiHelper.drawScaledText(pMatrixStack, message, this.x + 3, this.y + (this.height - 8) / 2, this.width - 6, j | MathHelper.ceil(this.alpha * 255.0F) << 24, !this.selected, TextAlignment.CENTER);
	}

	public boolean isSelected()
	{
		return this.selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
		this.setFGColor(selected ? 0x3F3F3F : 0xFFFFFF);
	}

}
