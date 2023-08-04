package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import giselle.jei_mekanism_multiblocks.client.IRecipeLayout;
import giselle.jei_mekanism_multiblocks.client.IRecipeLayoutHolder;
import giselle.jei_mekanism_multiblocks.client.IRecipeLogicStateListener;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;

@Mixin(value = RecipesGui.class, remap = false)
public class RecipesGuiMixin implements IRecipeLayoutHolder, IRecipeLogicStateListener
{
	@Shadow
	private final List<RecipeLayout<?>> recipeLayouts = new ArrayList<>();

	@Override
	public List<IRecipeLayout<?>> jei_mekanism_multiblocks$getRecipeLayouts()
	{
		List<IRecipeLayout<?>> list = new ArrayList<>();

		for (RecipeLayout<?> recipeLayout : this.recipeLayouts)
		{
			list.add((IRecipeLayout<?>) recipeLayout);
		}

		return list;
	}

	@Override
	public void jei_mekanism_multiblocks$onStateChange()
	{
		((RecipesGui) (Object) (this)).onStateChange();
	}

}
