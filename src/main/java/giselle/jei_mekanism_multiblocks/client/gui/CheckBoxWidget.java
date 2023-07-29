package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class CheckBoxWidget extends AbstractButton
{
	private boolean selected;
	private Consumer<Boolean> setter;

	public CheckBoxWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected)
	{
		this(pX, pY, pWidth, pHeight, pMessage, pSelected, null);
	}

	public CheckBoxWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected, Consumer<Boolean> setter)
	{
		super(pX, pY, pWidth, pHeight, pMessage);
		this.selected = pSelected;
		this.setter = setter;
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

			if (this.setter != null)
			{
				this.setter.accept(selected);
			}

		}

	}

	@Override
	@SuppressWarnings("deprecation")
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(GuiHelper.WIDGETS_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int checkerLength = this.height;
		AbstractGui.blit(pMatrixStack, this.x, this.y, checkerLength, checkerLength, 0.0F + (this.isHovered() ? 10.0F : 0.0F), 16.0F + (this.selected ? 10.0F : 0.0F), 10, 10, 256, 256);
		this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);

		GuiHelper.drawScaledText(pMatrixStack, this.getMessage(), this.x + checkerLength, this.y, this.x + this.width - checkerLength, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24, true);
	}
}
