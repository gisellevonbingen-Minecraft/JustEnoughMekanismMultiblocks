package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import giselle.jei_mekanism_multiblocks.client.IRecipeLayoutHolder;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;

@Mixin(value = RecipesGui.class, remap = false)
public class RecipesGuiMixin implements IRecipeLayoutHolder
{
	@Shadow
	private final List<RecipeLayout<?>> recipeLayouts = new ArrayList<>();

	@Override
	public List<RecipeLayout<?>> jei_mekanism_multiblocks$getRecipeLayouts()
	{
		return this.recipeLayouts;
	}

}
