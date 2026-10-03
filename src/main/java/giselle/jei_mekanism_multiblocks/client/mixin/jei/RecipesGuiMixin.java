package giselle.jei_mekanism_multiblocks.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import giselle.jei_mekanism_multiblocks.client.IRecipeLogicStateListener;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.ILookupState;

@Mixin(value = RecipesGui.class, remap = false)
public abstract class RecipesGuiMixin implements IRecipeLogicStateListener
{
	@Shadow
	private IRecipeGuiLogic logic;

	@Unique
	private boolean jei_mekanism_multiblocks$onStateChangeWasCaught;

	@Override
	public void jei_mekanism_multiblocks$onStateChange()
	{
		if (!jei_mekanism_multiblocks$onStateChangeWasCaught)
		{
			try
			{
				if (this.logic instanceof RecipeGuiLogicAccessor accessor)
				{
					ILookupState state = accessor.getState();
					accessor.invokeSetState(state, false);
				}

			}
			catch (Throwable e)
			{
				this.jei_mekanism_multiblocks$onStateChangeWasCaught = true;
				JEI_MekanismMultiblocks.LOGGER.error("", e);
			}

		}

	}

}
