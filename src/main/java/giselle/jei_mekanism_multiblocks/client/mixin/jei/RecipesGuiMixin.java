package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import giselle.jei_mekanism_multiblocks.client.IRecipeLogicStateListener;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;

@Mixin(value = RecipesGui.class, remap = false)
public class RecipesGuiMixin implements IRecipeLogicStateListener
{
	@Shadow
	private RecipeGuiLayouts layouts;

	@Shadow
	private void updateLayout()
	{
		throw new AssertionError();
	}

	@Override
	public void jei_mekanism_multiblocks$onStateChange()
	{
		this.updateLayout();
	}

}
