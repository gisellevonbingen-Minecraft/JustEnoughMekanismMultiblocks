package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import giselle.jei_mekanism_multiblocks.client.ICategoryHolder;
import giselle.jei_mekanism_multiblocks.client.IRecipeLogicStateListener;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;

@Mixin(value = RecipesGui.class, remap = false)
public abstract class RecipesGuiMixin implements IRecipeLogicStateListener, ICategoryHolder
{
	@Shadow
	private IRecipeGuiLogic logic;
	@Shadow
	private RecipeGuiLayouts layouts;

	@Unique
	private boolean jei_mekanism_multiblocks$onStateChangeWasCaught;
	@Unique
	private boolean jei_mekanism_multiblocks$getRecipeLayoutsWasCaught;

	@Override
	public void jei_mekanism_multiblocks$onStateChange()
	{
		if (!jei_mekanism_multiblocks$onStateChangeWasCaught)
		{
			try
			{
				var accessor = (RecipeGuiLogicAccessor) this.logic;
				accessor.invokeSetState(accessor.getState(), false);
			}
			catch (Throwable e)
			{
				this.jei_mekanism_multiblocks$onStateChangeWasCaught = true;
				JEI_MekanismMultiblocks.LOGGER.error("", e);
			}

		}

	}

	@Override
	public List<IRecipeLayoutDrawable<?>> jei_mekanism_multiblocks$getRecipeLayouts()
	{
		if (!jei_mekanism_multiblocks$getRecipeLayoutsWasCaught)
		{
			try
			{
				var list = new ArrayList<IRecipeLayoutDrawable<?>>();

				for (var w : ((RecipeGuiLayoutsAccessor) this.layouts).getRecipeLayoutsWithButtons())
				{
					list.add(w.recipeLayout());
				}

				return list;
			}
			catch (Throwable e)
			{
				this.jei_mekanism_multiblocks$getRecipeLayoutsWasCaught = true;
				JEI_MekanismMultiblocks.LOGGER.error("", e);
			}

		}

		return Collections.emptyList();
	}

}
