package giselle.jei_mekanism_multiblocks.client;

import mezz.jei.api.recipe.category.IRecipeCategory;

public interface IRecipeLayout<T>
{
	T jei_mekanism_multiblocks$getRecipe();

	IRecipeCategory<?> jei_mekanism_multiblocks$getRecipeCategory();

	boolean jei_mekanism_multiblocks$isMouseOver(double mouseX, double mouseY);

	int jei_mekanism_multiblocks$getPosX();

	int jei_mekanism_multiblocks$getPosY();
}
