package giselle.jei_mekanism_multiblocks.client.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.JemiRecipe;
import net.minecraft.resources.ResourceLocation;

public class EMIMultiblockRecipe<RECIPE> implements EmiRecipe
{
	private final JemiRecipe<RECIPE> original;
	private JemiRecipe<RECIPE> inputOverride;

	public EMIMultiblockRecipe(JemiRecipe<RECIPE> original)
	{
		this.original = original;
		this.inputOverride = original;
	}

	public RECIPE getRecipe()
	{
		return this.original.recipe;
	}

	public void invalidate()
	{
		this.inputOverride = null;
	}

	@Override
	public EmiRecipeCategory getCategory()
	{
		return this.original.getCategory();
	}

	@Override
	public @Nullable ResourceLocation getId()
	{
		return this.original.getId();
	}

	@Override
	public List<EmiIngredient> getInputs()
	{
		if (this.inputOverride == null)
		{
			this.inputOverride = new JemiRecipe<>(this.original.recipeCategory, this.original.category, this.original.recipe);
		}

		return this.inputOverride.getInputs();
	}

	@Override
	public List<EmiStack> getOutputs()
	{
		return this.original.getOutputs();
	}

	@Override
	public int getDisplayWidth()
	{
		return this.original.getDisplayWidth();
	}

	@Override
	public int getDisplayHeight()
	{
		return this.original.getDisplayHeight();
	}

	@Override
	public void addWidgets(WidgetHolder widgets)
	{
		this.original.addWidgets(widgets);
	}

	@Override
	public boolean supportsRecipeTree()
	{
		return true;
	}

}
