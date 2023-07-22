package giselle.jei_mekanism_multiblocks.client.jei;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import giselle.jei_mekanism_multiblocks.client.jei.category.DynamicTankCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.FissionReactorCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.ThermalEvaporationPlantCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.ThermoelectricBoilerCategory;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
	private static JeiPlugin INSTANCE = null;

	public static JeiPlugin instance()
	{
		return INSTANCE;
	}

	@Override
	public ResourceLocation getPluginUid()
	{
		return JEI_MekanismMultiblocks.rl("jei_plugin");
	}

	private final List<MultiblockCategory<?>> categories;

	public JeiPlugin()
	{
		INSTANCE = this;
		this.categories = new ArrayList<>();
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IModPlugin.super.registerCategories(registration);

		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		this.categories.clear();
		this.categories.add(new DynamicTankCategory(guiHelper));
		this.categories.add(new ThermalEvaporationPlantCategory(guiHelper));
		this.categories.add(new ThermoelectricBoilerCategory(guiHelper));

		if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
		{
			this.categories.add(new FissionReactorCategory(guiHelper));
		}

		for (MultiblockCategory<?> category : this.getCategories())
		{
			registration.addRecipeCategories(category);
		}

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		IModPlugin.super.registerRecipeCatalysts(registration);

		for (MultiblockCategory<?> category : this.getCategories())
		{
			category.registerRecipeCatalysts(registration);
		}

	}

	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		IModPlugin.super.registerRecipes(registration);

		for (MultiblockCategory<?> category : this.getCategories())
		{
			try
			{
				Object recipe = category.getRecipeClass().getDeclaredConstructor().newInstance();
				registration.addRecipes(Collections.singleton(recipe), category.getUid());
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				e.printStackTrace();
			}

		}

	}

	public List<MultiblockCategory<?>> getCategories()
	{
		return Collections.unmodifiableList(this.categories);
	}

}
