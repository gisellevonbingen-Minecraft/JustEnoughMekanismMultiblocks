package giselle.jei_mekanism_multiblocks.client.mixin.emi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.jemi.JemiCategory;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiRecipe;
import giselle.jei_mekanism_multiblocks.client.emi.EMIMultiblockRecipe;
import giselle.jei_mekanism_multiblocks.client.jei.JeiPlugin;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;

@Mixin(value = JemiPlugin.class, remap = false)
public abstract class JemiPluginMixin
{
	@Inject(method = "register", remap = false, at = @At(value = "TAIL"))
	private void register(EmiRegistry registry, CallbackInfo ci)
	{
		this.jei_mekanism_multiblocks$register(registry);
	}

	private void jei_mekanism_multiblocks$register(EmiRegistry registry)
	{
		for (MultiblockCategory<? extends MultiblockWidget> jeiCategory : JeiPlugin.instance().getCategories())
		{
			registry.addRecipe(this.createRecipe(jeiCategory));
		}

	}

	private <WIDGET extends MultiblockWidget> EMIMultiblockRecipe<WIDGET> createRecipe(MultiblockCategory<WIDGET> jeiCategory)
	{
		WIDGET jeiRecipe = JeiPlugin.instance().createWidget(jeiCategory);
		JemiCategory emiCategory = new JemiCategory(jeiCategory);
		EMIMultiblockRecipe<WIDGET> emiRecipe = new EMIMultiblockRecipe<>(new JemiRecipe<>(emiCategory, jeiCategory, jeiRecipe));
		jeiRecipe.addChangedHandler(w -> emiRecipe.invalidate());
		return emiRecipe;
	}

}
