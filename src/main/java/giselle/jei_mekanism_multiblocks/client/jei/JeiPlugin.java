package giselle.jei_mekanism_multiblocks.client.jei;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import giselle.jei_mekanism_multiblocks.client.JEI_MekanismMultiblocks_Client;
import giselle.jei_mekanism_multiblocks.client.SavedData;
import giselle.jei_mekanism_multiblocks.client.jei.category.BoilerCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.DynamicTankCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.EvaporationPlantCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.FissionReactorCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.FusionReactorCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.LasersCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.MatrixCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.SPSCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.TurbineCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.better_fusion.BetterFusionReactorCategory;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.config.ClientConfig;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.nbt.CompoundNBT;
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

	private final Map<ResourceLocation, MultiblockCategory<? extends MultiblockWidget>> categoryMap;
	private final List<MultiblockCategory<? extends MultiblockWidget>> categoryList;
	private final Map<ResourceLocation, MultiblockWidget> widgetMap;
	private final List<MultiblockWidget> widgetList;
	private IJeiRuntime jeiRuntime;

	public JeiPlugin()
	{
		INSTANCE = this;
		this.categoryMap = new HashMap<>();
		this.categoryList = new ArrayList<>();
		this.widgetMap = new HashMap<>();
		this.widgetList = new ArrayList<>();
		this.jeiRuntime = null;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		IModPlugin.super.onRuntimeAvailable(jeiRuntime);

		this.jeiRuntime = jeiRuntime;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IModPlugin.super.registerCategories(registration);

		ClientConfig config = JEI_MekanismMultiblocks_Config.CLIENT;
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		this.categoryMap.clear();
		this.categoryList.clear();
		this.widgetMap.clear();
		this.widgetList.clear();
		this.addCategory(config.dynamicTankVisible, () -> new DynamicTankCategory(guiHelper));
		this.addCategory(config.evaporationPlantVisible, () -> new EvaporationPlantCategory(guiHelper));
		this.addCategory(config.boilerVisible, () -> new BoilerCategory(guiHelper));
		this.addCategory(config.spsVisible, () -> new SPSCategory(guiHelper));
		this.addCategory(config.matrixVisible, () -> new MatrixCategory(guiHelper));
		this.addCategory(config.lasersVisible, () -> new LasersCategory(guiHelper));

		if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
		{
			this.addCategory(config.turbineVisible, () -> new TurbineCategory(guiHelper));
			this.addCategory(config.fissionReactorVisible, () -> new FissionReactorCategory(guiHelper));
			this.addCategory(config.fusionReactorVisible, () -> new FusionReactorCategory(guiHelper));
		}

		if (JEI_MekanismMultiblocks.BetterFusionReactorLoaded)
		{
			this.addCategory(config.betterFusionVisible, () -> new BetterFusionReactorCategory(guiHelper));
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
			CATEGOERY category = constructor.get();
			this.categoryMap.put(category.getUid(), category);
			this.categoryList.add(category);
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
			registration.addRecipes(Arrays.asList(this.createWidget(category)), category.getUid());

		}

	}

	public <WIDGET extends MultiblockWidget> WIDGET createWidget(MultiblockCategory<WIDGET> category)
	{
		try
		{
			WIDGET widget = category.getRecipeClass().getDeclaredConstructor().newInstance();

			if (SavedData.hasMultiblock(category.getUid()))
			{
				widget.load(SavedData.getMultiblock(category.getUid()));
			}

			widget.addChangedHandler(w -> this.onWidgetChanged(category, widget));

			this.widgetMap.put(category.getUid(), widget);
			this.widgetList.add(widget);
			return widget;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException("Category: " + category.getUid(), e);
		}

	}

	private void onWidgetChanged(MultiblockCategory<?> category, MultiblockWidget widget)
	{
		CompoundNBT tag = new CompoundNBT();
		widget.save(tag);

		SavedData.setMultiblockData(category.getUid(), tag);
		JEI_MekanismMultiblocks_Client.markNeedSave();
	}

	public IJeiRuntime getJeiRuntime()
	{
		return this.jeiRuntime;
	}

	@SuppressWarnings("unchecked")
	public <RECIPE_TYPE extends MultiblockWidget> MultiblockCategory<? extends RECIPE_TYPE> getCategory(ResourceLocation uid)
	{
		return (MultiblockCategory<? extends RECIPE_TYPE>) this.categoryMap.get(uid);
	}

	public List<MultiblockCategory<? extends MultiblockWidget>> getCategories()
	{
		return Collections.unmodifiableList(this.categoryList);
	}

	@SuppressWarnings("unchecked")
	public <RECIPE_TYPE extends MultiblockWidget> RECIPE_TYPE getWidget(ResourceLocation uid)
	{
		return (RECIPE_TYPE) this.widgetMap.get(uid);
	}

	public List<MultiblockWidget> getWidgets()
	{
		return Collections.unmodifiableList(this.widgetList);
	}

}
