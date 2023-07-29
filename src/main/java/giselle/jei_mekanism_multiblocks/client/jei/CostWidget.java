package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CostWidget extends Widget
{
	private final ItemStack itemStack;
	private final boolean hasCountExpressionComponent;
	private final StringTextComponent countExpressionComponent;
	private final StringTextComponent countTotalComponent;
	private ITextComponent[] headTooltips;
	private ITextComponent[] tailTooltips;

	public CostWidget(int pX, int pY, int pWidth, int pHeight, ItemStack itemStack)
	{
		super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY);
		this.itemStack = itemStack;
		this.packedFGColor = 0x3F3F3F;

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
		boolean shadow = false;

		if (this.hasCountExpressionComponent)
		{
			GuiHelper.drawScaledText(pMatrixStack, this.countExpressionComponent, textX, textY, textWidth, color, shadow);
			GuiHelper.drawScaledText(pMatrixStack, this.countTotalComponent, textX, textY + font.lineHeight, textWidth, color, shadow);
		}
		else
		{
			GuiHelper.drawScaledText(pMatrixStack, this.countTotalComponent, textX, textY + font.lineHeight / 2, textWidth, color, shadow);
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
			List<ITextComponent> tooltip = new ArrayList<>();
			ITextComponent[] headTooltips = this.getHeadTooltips();

			if (headTooltips != null)
			{
				tooltip.addAll(Arrays.asList(headTooltips));
			}

			tooltip.addAll(minecraft.screen.getTooltipFromItem(this.getItemStack()));

			ITextComponent[] tailTooltips = this.getTailTooltips();

			if (tailTooltips != null)
			{
				tooltip.addAll(Arrays.asList(tailTooltips));
			}

			minecraft.screen.renderWrappedToolTip(pPoseStack, tooltip, pMouseX, pMouseY, minecraft.font);
		}

	}

	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	public ITextComponent[] getHeadTooltips()
	{
		return this.headTooltips;
	}

	public void setHeadTooltips(ITextComponent... tooltips)
	{
		this.headTooltips = tooltips;
	}

	public ITextComponent[] getTailTooltips()
	{
		return this.tailTooltips;
	}

	public void setTailTooltips(ITextComponent... tooltips)
	{
		this.tailTooltips = tooltips;
	}

}
