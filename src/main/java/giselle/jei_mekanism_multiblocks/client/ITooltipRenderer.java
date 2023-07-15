package giselle.jei_mekanism_multiblocks.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.item.ItemStack;

public interface ITooltipRenderer
{
	void jei_mekanism_multiblocks$renderTooltip(MatrixStack pMatrixStack, ItemStack pItemStack, int pMouseX, int pMouseY);
}
