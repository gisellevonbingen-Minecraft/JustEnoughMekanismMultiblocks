package giselle.jei_mekanism_multiblocks.client.mixin.emi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiRecipe;
import giselle.jei_mekanism_multiblocks.client.emi.EMIMultiblockRecipe;
import giselle.jei_mekanism_multiblocks.client.jei.JeiPlugin;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.api.recipe.category.IRecipeCategory;

@Mixin(value = JemiPlugin.class, remap = false)
public abstract class JemiPluginMixin
{
	@Shadow(remap = false)
	private static Map<EmiRecipeCategory, IRecipeCategory<?>> CATEGORY_MAP;

	@Inject(method = "register", remap = false, at = @At(value = "TAIL"))
	private void register(EmiRegistry registry, CallbackInfo ci)
	{
		this.jei_mekanism_multiblocks$register(registry);
	}

	private void jei_mekanism_multiblocks$register(EmiRegistry registry)
	{
		Map<IRecipeCategory<?>, EmiRecipeCategory> reverse = new HashMap<>();

		for (Entry<EmiRecipeCategory, IRecipeCategory<?>> entry : CATEGORY_MAP.entrySet())
		{
			reverse.put(entry.getValue(), entry.getKey());
		}

		for (MultiblockCategory<? extends MultiblockWidget> jeiCategory : JeiPlugin.instance().getCategories())
		{
			EmiRecipeCategory emiCategory = reverse.get(jeiCategory);

			if (emiCategory == null)
			{
				JEI_MekanismMultiblocks.LOGGER.error(
						"Skipping EMI recipe for unregistered JEI category: {}",
						jeiCategory.getClass().getName());
				continue;
			}

			registry.addRecipe(this.createRecipe(jeiCategory, emiCategory));
		}

	}

	private <WIDGET extends MultiblockWidget> EMIMultiblockRecipe<WIDGET> createRecipe(MultiblockCategory<WIDGET> jeiCategory, EmiRecipeCategory emiCategory)
	{
		WIDGET jeiRecipe = JeiPlugin.instance().createWidget(jeiCategory);
		EMIMultiblockRecipe<WIDGET> emiRecipe = new EMIMultiblockRecipe<>(new JemiRecipe<>(emiCategory, jeiCategory, jeiRecipe));
		jeiRecipe.addChangedHandler(w -> emiRecipe.invalidate());
		return emiRecipe;
	}

}
