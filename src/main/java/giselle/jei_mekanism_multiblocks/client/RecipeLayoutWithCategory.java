package giselle.jei_mekanism_multiblocks.client;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeLayoutWithCategory<CATEGORY extends IRecipeCategory<RECIPE>, RECIPE>
{
	private IRecipeLayoutDrawable<RECIPE> recipeLayout;
	private CATEGORY recipeCategory;

	public RecipeLayoutWithCategory(IRecipeLayoutDrawable<RECIPE> recipeLayout, CATEGORY recipeCategory)
	{
		this.recipeLayout = recipeLayout;
		this.recipeCategory = recipeCategory;
	}

	public IRecipeLayoutDrawable<RECIPE> getRecipeLayout()
	{
		return this.recipeLayout;
	}

	public CATEGORY getRecipeCategory()
	{
		return recipeCategory;
	}

}
