package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;

import giselle.jei_mekanism_multiblocks.client.IRecipeLayout;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;

@Mixin(value = RecipeLayout.class, remap = false)
public class RecipeLayoutMixin<T> implements IRecipeLayout<T>
{
	@SuppressWarnings("unchecked")
	@Override
	public T jei_mekanism_multiblocks$getRecipe()
	{
		return ((RecipeLayout<T>) (Object) this).getRecipe();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IRecipeCategory<?> jei_mekanism_multiblocks$getRecipeCategory()
	{
		return ((RecipeLayout<T>) (Object) this).getRecipeCategory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean jei_mekanism_multiblocks$isMouseOver(double mouseX, double mouseY)
	{
		return ((RecipeLayout<T>) (Object) this).isMouseOver(mouseX, mouseY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int jei_mekanism_multiblocks$getPosX()
	{
		return ((RecipeLayout<T>) (Object) this).getPosX();
	}

	@SuppressWarnings("unchecked")
	@Override
	public int jei_mekanism_multiblocks$getPosY()
	{
		return ((RecipeLayout<T>) (Object) this).getPosY();
	}

}
