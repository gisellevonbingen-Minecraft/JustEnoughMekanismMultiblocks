package giselle.jei_mekanism_multiblocks.client;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;

public class RecipeLayoutWithCategory<CATEGORY extends IRecipeCategory<RECIPE>, RECIPE>
{
	private RecipeLayout<RECIPE> recipeLayout;
	private CATEGORY recipeCategory;

	public RecipeLayoutWithCategory(RecipeLayout<RECIPE> recipeLayout, CATEGORY recipeCategory)
	{
		this.recipeLayout = recipeLayout;
		this.recipeCategory = recipeCategory;
	}

	public RecipeLayout<RECIPE> getRecipeLayout()
	{
		return this.recipeLayout;
	}

	public CATEGORY getRecipeCategory()
	{
		return recipeCategory;
	}

}
