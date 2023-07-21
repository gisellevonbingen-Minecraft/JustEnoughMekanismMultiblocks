package giselle.jei_mekanism_multiblocks.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.ITooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;

public class CostWidget extends Widget
{
	private final ItemStack itemStack;
	private final boolean hasCountExpressionComponent;
	private final StringTextComponent countExpressionComponent;
	private final StringTextComponent countTotalComponent;

	public CostWidget(int pX, int pY, int pWidth, int pHeight, ItemStack itemStack)
	{
		super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY);
		this.itemStack = itemStack;

		int count = itemStack.getCount();
		int maxStackSize = itemStack.getMaxStackSize();
		int stacks = maxStackSize > 1 ? count / maxStackSize : 0;
		int remains = maxStackSize > 1 ? count % maxStackSize : 0;

		StringBuilder builder = new StringBuilder();

		if (stacks > 0)
		{
			builder.append(stacks).append("x").append(maxStackSize);

			if (remains > 0)
			{
				builder.append(" + ");
			}

		}

		if (remains > 0)
		{
			builder.append(remains);
		}

		this.hasCountExpressionComponent = stacks > 0;
		this.countExpressionComponent = new StringTextComponent(builder.toString());
		this.countTotalComponent = new StringTextComponent("=" + count);
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		FontRenderer font = minecraft.font;

		int itemX = this.x + 0;
		int itemY = this.y + 2;
		ItemStack itemStack = this.getItemStack();
		Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
		vector4f.transform(pMatrixStack.last().pose());
		itemRenderer.renderGuiItem(itemStack, (int) vector4f.x() + itemX, (int) vector4f.y() + itemY);

		int textX = itemX + 18;
		int textY = itemY;
		int textWidth = this.width - 18;
		int color = this.getFGColor();

		if (this.hasCountExpressionComponent)
		{
			GuiHelper.drawTextScaledShadow(pMatrixStack, this.countExpressionComponent, textX, textY, textWidth, color);
			GuiHelper.drawTextScaledShadow(pMatrixStack, this.countTotalComponent, textX, textY + font.lineHeight, textWidth, color);
		}
		else
		{
			GuiHelper.drawTextScaledShadow(pMatrixStack, this.countTotalComponent, textX, textY + font.lineHeight / 2, textWidth, color);
		}

		this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
	}

	@Override
	public void renderToolTip(MatrixStack pPoseStack, int pMouseX, int pMouseY)
	{
		super.renderToolTip(pPoseStack, pMouseX, pMouseY);

		if (this.visible && this.isHovered())
		{
			Minecraft minecraft = Minecraft.getInstance();

			if (minecraft.screen instanceof ITooltipRenderer)
			{
				((ITooltipRenderer) minecraft.screen).jei_mekanism_multiblocks$renderTooltip(pPoseStack, this.getItemStack(), pMouseX, pMouseY);
			}

		}

	}

	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

}
