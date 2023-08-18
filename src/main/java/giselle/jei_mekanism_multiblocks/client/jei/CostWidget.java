package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

public class CostWidget extends AbstractWidget
{
	private final ItemStack itemStack;
	private final boolean hasCountExpressionComponent;
	private final Component countExpressionComponent;
	private final Component countTotalComponent;
	private Component[] headTooltip;
	private Component[] tailTooltip;

	public CostWidget(int pX, int pY, int pWidth, int pHeight, ItemStack itemStack)
	{
		super(pX, pY, pWidth, pHeight, TextComponent.EMPTY);
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
		this.countExpressionComponent = new TextComponent(builder.toString());
		this.countTotalComponent = new TextComponent("=").append(TextUtils.format(count));
		this.headTooltip = new Component[0];
		this.tailTooltip = new Component[0];
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Font font = minecraft.font;

		Rect2i itemRect = this.getItemBounds();
		ItemStack itemStack = this.getItemStack();
		Vector4f vector4f = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
		vector4f.transform(pPoseStack.last().pose());
		itemRenderer.renderAndDecorateItem(null, itemStack, (int) vector4f.x() + itemRect.getX(), (int) vector4f.y() + itemRect.getY(), 0);

		int textX = itemRect.getX() + 18;
		int textY = itemRect.getY();
		int textWidth = this.width - textX;
		int color = this.getFGColor();
		boolean shadow = false;

		if (this.hasCountExpressionComponent)
		{
			GuiHelper.drawScaledText(pPoseStack, this.countExpressionComponent, textX, textY, textWidth, color, shadow);
			GuiHelper.drawScaledText(pPoseStack, this.countTotalComponent, textX, textY + font.lineHeight, textWidth, color, shadow);
		}
		else
		{
			GuiHelper.drawScaledText(pPoseStack, this.countTotalComponent, textX, textY + font.lineHeight / 2, textWidth, color, shadow);
		}

		this.renderToolTip(pPoseStack, pMouseX, pMouseY);
	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
	{
		super.renderToolTip(pPoseStack, pMouseX, pMouseY);

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen != null && this.visible && this.isHoveredOrFocused())
		{
			List<Component> tooltip = new ArrayList<>();
			tooltip.addAll(Arrays.asList(this.getHeadTooltip()));
			tooltip.addAll(minecraft.screen.getTooltipFromItem(this.getItemStack()));
			tooltip.addAll(Arrays.asList(this.getTailTooltip()));

			GuiHelper.renderComponentTooltip(pPoseStack, pMouseX, pMouseY, tooltip);
		}

	}

	public Rect2i getItemBounds()
	{
		int itemX = this.x + 2;
		int itemY = this.y + 2;
		return new Rect2i(itemX, itemY, 16, 16);
	}

	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	public Component[] getHeadTooltip()
	{
		return this.headTooltip.clone();
	}

	public void setHeadTooltip(Component... tooltip)
	{
		this.headTooltip = tooltip.clone();
	}

	public Component[] getTailTooltip()
	{
		return this.tailTooltip.clone();
	}

	public void setTailTooltip(Component... tooltip)
	{
		this.tailTooltip = tooltip.clone();
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
