package giselle.jei_mekanism_multiblocks.client;

import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeLayoutWithCategory<CATEGORY extends IRecipeCategory<RECIPE>, RECIPE>
{
	private IRecipeLayout<RECIPE> recipeLayout;
	private CATEGORY recipeCategory;

	public RecipeLayoutWithCategory(IRecipeLayout<RECIPE> recipeLayout, CATEGORY recipeCategory)
	{
		this.recipeLayout = recipeLayout;
		this.recipeCategory = recipeCategory;
	}

	public IRecipeLayout<RECIPE> getRecipeLayout()
	{
		return this.recipeLayout;
	}

	public CATEGORY getRecipeCategory()
	{
		return recipeCategory;
	}

}
