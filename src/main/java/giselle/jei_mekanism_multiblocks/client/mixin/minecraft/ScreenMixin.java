package giselle.jei_mekanism_multiblocks.client.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.ITooltipRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ITooltipRenderer
{
	@Shadow
	protected abstract void renderTooltip(MatrixStack pMatrixStack, ItemStack pItemStack, int pMouseX, int pMouseY);

	@Override
	public void jei_mekanism_multiblocks$renderTooltip(MatrixStack pMatrixStack, ItemStack pItemStack, int pMouseX, int pMouseY)
	{
		this.renderTooltip(pMatrixStack, pItemStack, pMouseX, pMouseY);
	}

}
