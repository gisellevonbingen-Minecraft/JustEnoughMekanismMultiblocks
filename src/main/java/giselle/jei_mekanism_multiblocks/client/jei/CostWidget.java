package giselle.jei_mekanism_multiblocks.client.jei;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CostWidget extends AbstractWidget
{
	private final ItemStack itemStack;
	private final boolean hasCountExpressionComponent;
	private final Component countExpressionComponent;
	private final Component countTotalComponent;
	private Component[] jeiHeadTooltip;
	private Component[] jeiTailTooltip;

	public CostWidget(int pX, int pY, int pWidth, int pHeight, ItemStack itemStack)
	{
		super(pX, pY, pWidth, pHeight, Component.empty());
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
				builder.append("+");
			}

		}

		if (remains > 0)
		{
			builder.append(remains);
		}

		this.hasCountExpressionComponent = stacks > 0;
		this.countExpressionComponent = Component.literal(builder.toString());
		this.countTotalComponent = Component.literal("=").append(TextUtils.format(count));
		this.jeiHeadTooltip = new Component[0];
		this.jeiTailTooltip = new Component[0];
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;

		Rect2i itemRect = this.getItemBounds();
		ItemStack itemStack = this.getItemStack();
		pGuiGraphics.renderFakeItem(itemStack, itemRect.getX(), itemRect.getY());

		int textX = itemRect.getX() + 18;
		int textY = itemRect.getY();
		int textWidth = this.width - textX;
		int color = this.getFGColor();
		boolean shadow = false;

		if (this.hasCountExpressionComponent)
		{
			GuiHelper.drawScaledText(pGuiGraphics, this.countExpressionComponent, textX, textY, textWidth, color, shadow);
			GuiHelper.drawScaledText(pGuiGraphics, this.countTotalComponent, textX, textY + font.lineHeight, textWidth, color, shadow);
		}
		else
		{
			GuiHelper.drawScaledText(pGuiGraphics, this.countTotalComponent, textX, textY + font.lineHeight / 2, textWidth, color, shadow);
		}

	}

	public Rect2i getItemBounds()
	{
		int itemX = this.getX() + 2;
		int itemY = this.getY() + 2;
		return new Rect2i(itemX, itemY, 16, 16);
	}

	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	public Component[] getJeiHeadTooltip()
	{
		return this.jeiHeadTooltip.clone();
	}

	public void setJeiHeadTooltip(Component... tooltip)
	{
		this.jeiHeadTooltip = tooltip.clone();
	}

	public Component[] getJeiTailTooltip()
	{
		return this.jeiTailTooltip.clone();
	}

	public void setJeiTailTooltip(Component... tooltip)
	{
		this.jeiTailTooltip = tooltip.clone();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
