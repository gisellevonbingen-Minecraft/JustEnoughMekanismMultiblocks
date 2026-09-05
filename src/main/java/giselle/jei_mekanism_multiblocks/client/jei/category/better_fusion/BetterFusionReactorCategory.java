package giselle.jei_mekanism_multiblocks.client.jei.category.better_fusion;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.Mod2IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.jei.JEI_MekanismMultiblocks_JeiPlugin;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import giselle.jei_mekanism_multiblocks.client.jei.category.LasersCategory;
import giselle.jei_mekanism_multiblocks.client.jei.category.LasersCategory.LaserWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.content.fusion.FusionReactorMultiblockData;
import igentuman.bfr.common.registries.BfrBlocks;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;

public class BetterFusionReactorCategory extends MultiblockCategory<BetterFusionReactorCategory.FusionReactorCategoryWidget>
{
	public static final RecipeType<BetterFusionReactorCategory.FusionReactorCategoryWidget> RECIPE_TYPE = createRecipeType(BetterFusionReactor.rl("fusion_reactor"), FusionReactorCategoryWidget.class);

	public BetterFusionReactorCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, new TranslationTextComponent("text.jei_mekanism_multiblocks.building.better_fusion_reactor"), BfrBlocks.FUSION_REACTOR_CONTROLLER.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(BfrBlocks.FUSION_REACTOR_CONTROLLER.getItemStack());
		consumer.accept(BfrBlocks.FUSION_REACTOR_FRAME.getItemStack());
		consumer.accept(BfrBlocks.FUSION_REACTOR_PORT.getItemStack());
		consumer.accept(BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(BfrBlocks.LASER_FOCUS_MATRIX.getItemStack());
		consumer.accept(BfrBlocks.REACTOR_GLASS.getItemStack());

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

	public static class FusionReactorCategoryWidget extends MultiblockWidget
	{
		private static final double burnTemperature = 100_000_000.0D;
		private static final double plasmaHeatCapacity = 100.0D;
		private static final double noBurningFactor = 10.0D;
		private static final FloatingLong requiredLaserEnergy = FloatingLong.create(burnTemperature).multiply(plasmaHeatCapacity).divide(noBurningFactor);

		protected CheckBoxWidget waterCooledCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected IntSliderWithButtons injectionRateWidget;

		public FusionReactorCategoryWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.waterCooledCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.water_cooled"), false));
			this.waterCooledCheckBox.addSelectedChangedHandler(this::onWaterCooledChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 0, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.injectionRateWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.injection_rate", new Mod2IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, 2, 2, FluidAttributes.BUCKET_VOLUME, 1)));
			this.injectionRateWidget.getSlider().addValueChangeHanlder(this::onInjectionRateChanged);

			this.updatePortsSliderLimit();
			this.updateInjectionRateInfoMessage();
		}

		@Override
		public void load(CompoundNBT tag)
		{
			super.load(tag);

			this.setWaterCooled(tag.getBoolean("WaterCooled"));
			this.setPortCount(tag.getInt("PortCount"));
			this.setLogicAdapterCount(tag.getInt("LogicAdapterCount"));
			this.setInjectionRate(tag.getInt("InjectionRate"));
		}

		@Override
		public void save(CompoundNBT tag)
		{
			super.save(tag);

			tag.putBoolean("WaterCooled", this.isWaterCooled());
			tag.putInt("PortCount", this.getPortCount());
			tag.putInt("LogicAdapterCount", this.getLogicAdapterCount());
			tag.putInt("InjectionRate", this.getInjectionRate());
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
			int minPorts = portsSlider.getMinValue();
			int ports = portsSlider.getValue();
			portsSlider.setMinValue(this.isWaterCooled() ? 4 : 2);
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setValue(ports + (portsSlider.getMinValue() - minPorts));

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

		protected void onInjectionRateChanged(int injectionRate)
		{
			this.markNeedUpdate();
			this.updateInjectionRateInfoMessage();
		}

		protected void onWaterCooledChanged(boolean waterCooled)
		{
			this.updatePortsSliderLimit();

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
			int glasses = 0;

			if (this.isUseGlass())
			{
				frames = corners;
				glasses = sides;
			}
			else
			{
				frames = corners + sides;
				glasses = 0;
			}

			consumer.accept(new ItemStack(BfrBlocks.FUSION_REACTOR_CONTROLLER));
			consumer.accept(new ItemStack(BfrBlocks.FUSION_REACTOR_FRAME, frames));
			consumer.accept(new ItemStack(BfrBlocks.FUSION_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(BfrBlocks.LASER_FOCUS_MATRIX));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
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

			// https://github.com/igentuman/Better-Fusion-Reactor/blob/174d3d5b3f6082bbf0951e8d830d69e48980a7eb/src/main/java/igentuman/bfr/common/content/fusion/BFReactorMultiblockData.java#L665C8-L665C9
			FloatingLong energyFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(2).divide(3);
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
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.passive_generation"), new TranslationTextComponent("%s/t", EnergyDisplay.of(passiveGeneration).getTextComponent())));

			if (steamProduction > 0L)
			{
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_production"), VolumeTextHelper.formatMBt(steamProduction)));
			}

			ResultWidget requiredLaserEnergyWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.required_laser_energy"), EnergyDisplay.of(requiredLaserEnergy).getTextComponent());
			MultiblockCategory<? extends LaserWidget> lasersCategory = JEI_MekanismMultiblocks_JeiPlugin.instance().<LaserWidget> getCategory(LasersCategory.RECIPE_TYPE.getUid());

			if (lasersCategory != null)
			{
				requiredLaserEnergyWidget.setTooltip(new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.click_to_simulate", lasersCategory.getName()));
				requiredLaserEnergyWidget.addPressHandler(this::onResultWidgetPress);
			}

			consumer.accept(requiredLaserEnergyWidget);
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.fuel_tank"), VolumeTextHelper.formatMB(fuelTank)));

			if (this.isWaterCooled())
			{
				TranslationTextComponent injectionRateTooltip = new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				ResultWidget watTankWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMB(waterTank));
				watTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(watTankWidget);
				ResultWidget steamTankWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(steamTank));
				steamTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(steamTankWidget);
			}

			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.energy_capacity"), EnergyDisplay.of(FloatingLong.createConst(1_000_000_000)).getTextComponent()));
		}

		private void onResultWidgetPress(ResultWidget widget)
		{
			LaserWidget lasers = JEI_MekanismMultiblocks_JeiPlugin.instance().getWidget(LasersCategory.RECIPE_TYPE.getUid());
			lasers.setTargetEnergy(requiredLaserEnergy.doubleValue());
			lasers.showResultPanel();

			JEI_MekanismMultiblocks_JeiPlugin.instance().getJeiRuntime().getRecipesGui().showCategories(Arrays.asList(LasersCategory.RECIPE_TYPE.getUid()));
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

		@Override
		public Block getGlassBlock()
		{
			return BfrBlocks.REACTOR_GLASS.getBlock();
		}

	}

}
