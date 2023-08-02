package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.List;
import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.Mod2IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;

public class FusionReactorCategory extends MultiblockCategory<FusionReactorCategory.FusionReactorCategoryWidget>
{
	public FusionReactorCategory(IGuiHelper helper)
	{
		super(helper, "fusion_reactor", helper.createDrawableIngredient(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getItemStack()));
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_FRAME.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_PORT.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(GeneratorsBlocks.LASER_FOCUS_MATRIX.getItemStack());
		consumer.accept(GeneratorsBlocks.REACTOR_GLASS.getItemStack());

		List<Gas> fusionFuelGases = GeneratorTags.Gases.FUSION_FUEL.getValues();

		if (fusionFuelGases.size() > 0)
		{
			Gas fusionFuelGas = fusionFuelGases.get(0);
			long capacity = MekanismGeneratorsConfig.generators.hohlraumMaxGas.get();
			consumer.accept(ChemicalUtil.getFilledVariant(GeneratorsItems.HOHLRAUM.getItemStack(), capacity, fusionFuelGas));
		}
		else
		{
			consumer.accept(GeneratorsItems.HOHLRAUM.getItemStack());
		}

	}

	@Override
	public void setIngredients(FusionReactorCategoryWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends FusionReactorCategoryWidget> getRecipeClass()
	{
		return FusionReactorCategoryWidget.class;
	}

	public static class FusionReactorCategoryWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useReactorGlassCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected IntSliderWithButtons injectionRateWidget;
		protected CheckBoxWidget waterCooledCheckBox;

		public FusionReactorCategoryWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useReactorGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.REACTOR_GLASS.getItemStack().getHoverName()), true));
			this.useReactorGlassCheckBox.addSelectedChangedHandler(this::onUseReactorGlassChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 1, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.injectionRateWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.injection_rate", new Mod2IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, 2, 2, FluidAttributes.BUCKET_VOLUME, 1)));
			this.injectionRateWidget.getSlider().addValueChangeHanlder(this::onInjectionRateChanged);
			consumer.accept(this.waterCooledCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.water_cooled"), false));
			this.waterCooledCheckBox.addSelectedChangedHandler(this::onWaterCooledChanged);

			this.updatePortsSliderLimit();
			this.setPortCount(4);
			this.setLogicAdapterCount(0);
			this.updateInjectionRateInfoMessage();
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsSliderLimit();
		}

		@Override
		public int getCornerBlocks()
		{
			// 36 Totals
			return 8 + 8 + 4 + 8 + 8;
		}

		@Override
		public int getSideBlocks()
		{
			// 30 Totals
			// 1 Controller
			// 1 Laser Focus Matrix
			return 5 + 4 + 12 + 4 + 5 - 2;
		}

		public void updatePortsSliderLimit()
		{
			IntSliderWidget portsSlider = this.portsWidget.getSlider();
			int valves = portsSlider.getValue();
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setValue(valves);

			this.updateLogicAdaptersSliderLimit();
		}

		public void updateLogicAdaptersSliderLimit()
		{
			IntSliderWidget adaptersSlider = this.logicAdaptersWidget.getSlider();
			int adapters = adaptersSlider.getValue();
			adaptersSlider.setMaxValue(this.getSideBlocks() - this.getPortCount());
			adaptersSlider.setValue(adapters);
		}

		protected void onPortsChanged(int ports)
		{
			this.updateLogicAdaptersSliderLimit();
			this.markNeedUpdate();
		}

		protected void onLogicAdaptersChanged(int logicAdapters)
		{
			this.markNeedUpdate();
		}

		protected void onUseReactorGlassChanged(boolean useReactorGlass)
		{
			this.markNeedUpdate();
		}

		protected void onInjectionRateChanged(int injectionRate)
		{
			this.markNeedUpdate();
			this.updateInjectionRateInfoMessage();
		}

		protected void onWaterCooledChanged(boolean waterCooled)
		{
			this.markNeedUpdate();
			this.updateInjectionRateInfoMessage();
		}

		public void updateInjectionRateInfoMessage()
		{
			if (this.isWaterCooled())
			{
				int limitedInjectionRate = Math.min(this.getInjectionRate(), FusionReactorMultiblockData.MAX_INJECTION);
				TranslationTextComponent tooltip = new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				this.waterCooledCheckBox.setTooltip(tooltip);
				this.injectionRateWidget.setTooltip(tooltip);
			}
			else
			{
				this.waterCooledCheckBox.setTooltip();
				this.injectionRateWidget.setTooltip();
			}

		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int ports = this.getPortCount();
			sides -= ports;
			int logicAdapter = this.getLogicAdapterCount();
			sides -= logicAdapter;

			int frames = 0;
			int reactorGlasses = 0;

			if (this.isUseReactorGlass())
			{
				frames = corners;
				reactorGlasses = sides;
			}
			else
			{
				frames = corners + sides;
				reactorGlasses = 0;
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_FRAME, frames));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(GeneratorsBlocks.LASER_FOCUS_MATRIX));
			consumer.accept(new ItemStack(GeneratorsBlocks.REACTOR_GLASS, reactorGlasses));
		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int injectionRate = this.getInjectionRate();
			int limitedInjectionRate = Math.min(injectionRate, FusionReactorMultiblockData.MAX_INJECTION);
			long waterTank = 1_000L * FluidAttributes.BUCKET_VOLUME * limitedInjectionRate;
			long steamTank = waterTank * 100L;
			long fuelTank = FluidAttributes.BUCKET_VOLUME;

			FloatingLong energyFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get();
			double casingThermalConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
			double casingTemp = energyFusionFuel.multiply(injectionRate).divide(casingThermalConductivity).doubleValue();
			long steamProduction = 0L;

			if (this.isWaterCooled())
			{
				double waterHeatingRatio = MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get();
				double wateredCasingTemp = energyFusionFuel.multiply(injectionRate).divide(casingThermalConductivity + waterHeatingRatio).doubleValue();
				double waterHeat = waterHeatingRatio * wateredCasingTemp;
				steamProduction = (long) (HeatUtils.getSteamEnergyEfficiency() * waterHeat / HeatUtils.getWaterThermalEnthalpy());
				steamProduction = Math.min(steamProduction, waterTank);

				double coolingHeat = steamProduction / HeatUtils.getSteamEnergyEfficiency() * HeatUtils.getWaterThermalEnthalpy();
				double coolingCasingTemp = coolingHeat / casingThermalConductivity;
				casingTemp -= coolingCasingTemp;
			}

			double fusionThermocoupleEfficiency = MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get();
			FloatingLong passiveGeneration = FloatingLong.create(fusionThermocoupleEfficiency * casingThermalConductivity * casingTemp);
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.passive_generation"), EnergyDisplay.of(passiveGeneration).getTextComponent()));

			if (steamProduction > 0L)
			{
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_production"), VolumeTextHelper.format(steamProduction, VolumeUnit.MILLI, "B/t")));
			}

			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.fuel_tank"), VolumeTextHelper.formatMilliBuckets(fuelTank)));

			if (this.isWaterCooled())
			{
				TranslationTextComponent injectionRateTooltip = new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				ResultWidget watTankWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMilliBuckets(waterTank));
				watTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(watTankWidget);
				ResultWidget steamTankWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMilliBuckets(steamTank));
				steamTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(steamTankWidget);
			}

		}

		public int getPortCount()
		{
			return this.portsWidget.getSlider().getValue();
		}

		public void setPortCount(int portCount)
		{
			this.portsWidget.getSlider().setValue(portCount);
		}

		public int getLogicAdapterCount()
		{
			return this.logicAdaptersWidget.getSlider().getValue();
		}

		public void setLogicAdapterCount(int logicAdapterCount)
		{
			this.logicAdaptersWidget.getSlider().setValue(logicAdapterCount);
		}

		public boolean isUseReactorGlass()
		{
			return this.useReactorGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useReactorGlass)
		{
			this.useReactorGlassCheckBox.setSelected(useReactorGlass);
		}

		public int getInjectionRate()
		{
			return this.injectionRateWidget.getSlider().getValue();
		}

		public void setInjectionRate(int injectionRate)
		{
			this.injectionRateWidget.getSlider().setValue(injectionRate);
		}

		public boolean isWaterCooled()
		{
			return this.waterCooledCheckBox.isSelected();
		}

		public void setWaterCooled(boolean waterCooled)
		{
			this.waterCooledCheckBox.setSelected(waterCooled);
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 5;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 5;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 5;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 5;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 5;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 5;
		}

	}

}
