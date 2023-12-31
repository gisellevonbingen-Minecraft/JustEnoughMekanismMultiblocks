package giselle.jei_mekanism_multiblocks.client.jei;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import giselle.jei_mekanism_multiblocks.client.jei.category.BoilerCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.DynamicTankCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.EvaporationPlantCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.FissionReactorCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.FusionReactorCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.MatrixCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.SPSCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.TurbineCategory;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.config.ClientConfig;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

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

		ClientConfig config = JEI_MekanismMultiblocks_Config.CLIENT;
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		this.categories.clear();
		this.addCategory(config.dynamicTankVisible, () -> new DynamicTankCategory(guiHelper));
		this.addCategory(config.evaporationPlantVisible, () -> new EvaporationPlantCategory(guiHelper));
		this.addCategory(config.boilerVisible, () -> new BoilerCategory(guiHelper));
		this.addCategory(config.spsVisible, () -> new SPSCategory(guiHelper));
		this.addCategory(config.matrixVisible, () -> new MatrixCategory(guiHelper));

		if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
		{
			this.addCategory(config.turbineVisible, () -> new TurbineCategory(guiHelper));
			this.addCategory(config.fissionReactorVisible, () -> new FissionReactorCategory(guiHelper));
			this.addCategory(config.fusionReactorVisible, () -> new FusionReactorCategory(guiHelper));
		}

		for (MultiblockCategory<?> category : this.getCategories())
		{
			registration.addRecipeCategories(category);
		}

	}

	private <CATEGOERY extends MultiblockCategory<?>> void addCategory(BooleanValue config, Supplier<CATEGOERY> constructor)
	{
		if (config.get())
		{
			this.categories.add(constructor.get());
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
				System.err.println("Exception - " + category.getUid());
				e.printStackTrace();
			}

		}

	}

	public List<MultiblockCategory<?>> getCategories()
	{
		return Collections.unmodifiableList(this.categories);
	}

}
