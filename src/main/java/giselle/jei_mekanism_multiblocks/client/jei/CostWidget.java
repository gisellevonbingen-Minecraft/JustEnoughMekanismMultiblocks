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
	private ITextComponent[] headTooltip;
	private ITextComponent[] tailTooltip;

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
		this.headTooltip = new ITextComponent[0];
		this.tailTooltip = new ITextComponent[0];
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		FontRenderer font = minecraft.font;

		int itemX = this.x + 2;
		int itemY = this.y + 2;
		ItemStack itemStack = this.getItemStack();
		Vector4f vector4f = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
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

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && this.visible && this.isHovered())
		{
			List<ITextComponent> tooltip = new ArrayList<>();
			tooltip.addAll(Arrays.asList(this.getHeadTooltip()));
			tooltip.addAll(minecraft.screen.getTooltipFromItem(this.getItemStack()));
			tooltip.addAll(Arrays.asList(this.getTailTooltip()));

			GuiHelper.renderComponentTooltip(pPoseStack, pMouseX, pMouseY, tooltip);
		}

	}

	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	public ITextComponent[] getHeadTooltip()
	{
		return this.headTooltip.clone();
	}

	public void setHeadTooltip(ITextComponent... tooltip)
	{
		this.headTooltip = tooltip.clone();
	}

	public ITextComponent[] getTailTooltip()
	{
		return this.tailTooltip.clone();
	}

	public void setTailTooltip(ITextComponent... tooltip)
	{
		this.tailTooltip = tooltip.clone();
	}

}
