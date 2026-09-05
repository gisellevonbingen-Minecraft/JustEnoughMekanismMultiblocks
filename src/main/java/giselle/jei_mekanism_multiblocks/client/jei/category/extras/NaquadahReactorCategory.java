package giselle.jei_mekanism_multiblocks.client.jei.category.extras;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.jerry.genextras.common.GenExtraLang;
import com.jerry.genextras.common.GeneratorExtraTags;
import com.jerry.genextras.common.config.GeneratorsExtraConfig;
import com.jerry.genextras.common.registries.GenExtraBlocks;
import com.jerry.genextras.common.registries.GenExtraItems;
import com.jerry.mekextras.MekanismExtras;

import giselle.jei_mekanism_multiblocks.client.TooltipHelper;
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
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.math.MathUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidType;

public class NaquadahReactorCategory extends MultiblockCategory<NaquadahReactorCategory.NaquadahReactor>
{
	public static final RecipeType<NaquadahReactorCategory.NaquadahReactor> RECIPE_TYPE = createRecipeType(MekanismExtras.rl("naquadah_reactor"), NaquadahReactor.class);

	public NaquadahReactorCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, GenExtraLang.NAQUADAH_REACTOR.translate(), new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_CONTROLLER));
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_CONTROLLER));
		consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_CASING));
		consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_PORT));
		consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_LOGIC_ADAPTER));
		consumer.accept(new ItemStack(GenExtraBlocks.LEAD_COATED_LASER_FOCUS_MATRIX));
		consumer.accept(new ItemStack(GeneratorsBlocks.REACTOR_GLASS));

		List<Holder<Chemical>> fusionFuelChemicals = MekanismAPI.CHEMICAL_REGISTRY.getTag(GeneratorExtraTags.Chemicals.NAQUADAH_URANIUM_FUEL).stream().flatMap(Named<Chemical>::stream).toList();

		for (Holder<Chemical> chemical : fusionFuelChemicals)
		{
			consumer.accept(ChemicalUtil.getFilledVariant(new ItemStack(GenExtraItems.NAQUADAH_HOHLRAUM.get()), chemical));
		}

	}

	public static class NaquadahReactor extends MultiblockWidget
	{
		private static final double burnTemperature = 400_000_000.0D;
		private static final double plasmaHeatCapacity = 100.0D;
		private static final double noBurningFactor = 10.0D;
		private static final long requiredLaserEnergy = MathUtils.clampToLong(burnTemperature * plasmaHeatCapacity / noBurningFactor);

		private static final double burnRatio = 1.0D;
		private static final double plasmaCaseConductivity = 0.2D;

		protected CheckBoxWidget waterCooledCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected IntSliderWithButtons injectionRateWidget;

		public NaquadahReactor()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.waterCooledCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.water_cooled"), false));
			this.waterCooledCheckBox.addSelectedChangedHandler(this::onWaterCooledChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 0, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.injectionRateWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.injection_rate", new Mod2IntSliderWidget(0, 0, 0, 0, Component.empty(), 0, 0, FluidType.BUCKET_VOLUME, 1)));
			this.injectionRateWidget.getSlider().addValueChangeHanlder(this::onInjectionRateChanged);

			this.updatePortsSliderLimit();
			this.updateInjectionRateSliderLimit();
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			this.setWaterCooled(tag.getBoolean("WaterCooled"));
			this.setPortCount(tag.getInt("PortCount"));
			this.setLogicAdapterCount(tag.getInt("LogicAdapterCount"));
			this.setInjectionRate(tag.getInt("InjectionRate"));
		}

		@Override
		public void save(CompoundTag tag)
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
			// 108 Totals
			return (24 + 16 + 8 + 4) * 2 + 4;
		}

		@Override
		public int getSideBlocks()
		{
			// 222 Totals
			// 1 Controller
			// 1 Laser Focus Matrix
			return (37 * 6) - 2;
		}

		public void updateInjectionRateSliderLimit()
		{
			IntSliderWidget injectionRateSlider = this.injectionRateWidget.getSlider();
			int injectionRate = injectionRateSlider.getValue();
			injectionRateSlider.setMinValue(this.getMinInjectionRate(this.isWaterCooled()));
			injectionRateSlider.setValue(injectionRate);

			this.updateInjectionRateInfoMessage();
		}

		public void updateLogicAdaptersSliderLimit()
		{
			IntSliderWidget adaptersSlider = this.logicAdaptersWidget.getSlider();
			int adapters = adaptersSlider.getValue();
			adaptersSlider.setMaxValue(this.getSideBlocks() - this.getPortCount());
			adaptersSlider.setValue(adapters);
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
			this.updateInjectionRateSliderLimit();

			this.markNeedUpdate();
		}

		public int getMinInjectionRate(boolean waterCooled)
		{
			double k = waterCooled ? GeneratorsExtraConfig.extraGenerators.reactorWaterHeatingRatio.get() : 0;
			double caseAirConductivity = GeneratorsExtraConfig.extraGenerators.reactorCasingThermalConductivity.get();
			double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k + caseAirConductivity) / (GeneratorsExtraConfig.extraGenerators.energyPerReactorFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
			return 2 * Mth.ceil(aMin / 2.0D);
		}

		public void updateInjectionRateInfoMessage()
		{
			if (this.isWaterCooled())
			{
				int limitedInjectionRate = Math.min(this.getInjectionRate(), FusionReactorMultiblockData.MAX_INJECTION);
				Tooltip tooltip = Tooltip.create(Component.translatable("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate));
				this.waterCooledCheckBox.setTooltip(tooltip);
				this.injectionRateWidget.setTooltip(tooltip);
			}
			else
			{
				this.waterCooledCheckBox.setTooltip(null);
				this.injectionRateWidget.setTooltip(null);
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

			consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_CONTROLLER));
			consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_CASING, frames));
			consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(GenExtraBlocks.NAQUADAH_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(GenExtraBlocks.LEAD_COATED_LASER_FOCUS_MATRIX));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int injectionRate = this.getInjectionRate();
			int limitedInjectionRate = Math.min(injectionRate, FusionReactorMultiblockData.MAX_INJECTION);
			long waterTank = GeneratorsExtraConfig.extraGenerators.reactorWaterPerInjection.get() * limitedInjectionRate;
			long steamTank = GeneratorsExtraConfig.extraGenerators.reactorSteamPerInjection.get() * limitedInjectionRate;
			long fuelTank = GeneratorsExtraConfig.extraGenerators.reactorFuelCapacity.get();

			long energyFusionFuel = GeneratorsExtraConfig.extraGenerators.energyPerReactorFuel.get();
			double casingThermalConductivity = GeneratorsExtraConfig.extraGenerators.reactorCasingThermalConductivity.get();
			double casingTemp = MathUtils.multiplyClamped(energyFusionFuel, injectionRate) / casingThermalConductivity;
			long steamProduction = 0L;

			if (this.isWaterCooled())
			{
				double waterHeatingRatio = GeneratorsExtraConfig.extraGenerators.reactorWaterHeatingRatio.get();
				double wateredCasingTemp = MathUtils.multiplyClamped(energyFusionFuel, injectionRate) / (casingThermalConductivity + waterHeatingRatio);
				double waterHeat = waterHeatingRatio * wateredCasingTemp;
				steamProduction = (long) (HeatUtils.getSteamEnergyEfficiency() * waterHeat / HeatUtils.getWaterThermalEnthalpy());
				steamProduction = Math.min(steamProduction, waterTank);

				double coolingHeat = steamProduction / HeatUtils.getSteamEnergyEfficiency() * HeatUtils.getWaterThermalEnthalpy();
				double coolingCasingTemp = coolingHeat / casingThermalConductivity;
				casingTemp -= coolingCasingTemp;
			}

			double fusionThermocoupleEfficiency = GeneratorsExtraConfig.extraGenerators.reactorThermocoupleEfficiency.get();
			double passiveGeneration = fusionThermocoupleEfficiency * casingThermalConductivity * casingTemp;
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.passive_generation"), Component.translatable("%s/t", EnergyDisplay.of(MathUtils.clampToLong(passiveGeneration)).getTextComponent())));

			if (steamProduction > 0L)
			{
				consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.pcs_production"), VolumeTextHelper.formatMBt(steamProduction)));
			}

			ResultWidget requiredLaserEnergyWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.required_laser_energy"), EnergyDisplay.of(requiredLaserEnergy).getTextComponent());
			MultiblockCategory<? extends LaserWidget> lasersCategory = JEI_MekanismMultiblocks_JeiPlugin.instance().getCategory(LasersCategory.RECIPE_TYPE);

			if (lasersCategory != null)
			{
				requiredLaserEnergyWidget.setTooltip(TooltipHelper.createMessageOnly(Component.translatable("text.jei_mekanism_multiblocks.tooltip.click_to_simulate", lasersCategory.getName())));
				requiredLaserEnergyWidget.addPressHandler(this::onResultWidgetPress);
			}

			consumer.accept(requiredLaserEnergyWidget);
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.fuel_tank"), VolumeTextHelper.formatMB(fuelTank)));

			if (this.isWaterCooled())
			{
				Component injectionRateTooltip = Component.translatable("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				ResultWidget watTankWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMB(waterTank));
				watTankWidget.setTooltip(TooltipHelper.createMessageOnly(injectionRateTooltip));
				consumer.accept(watTankWidget);
				ResultWidget steamTankWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(steamTank));
				steamTankWidget.setTooltip(TooltipHelper.createMessageOnly(injectionRateTooltip));
				consumer.accept(steamTankWidget);
			}

			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_capacity"), EnergyDisplay.of(GeneratorsExtraConfig.extraGenerators.reactorEnergyCapacity.get()).getTextComponent()));
		}

		private void onResultWidgetPress(ResultWidget widget)
		{
			LaserWidget lasers = JEI_MekanismMultiblocks_JeiPlugin.instance().getWidget(LasersCategory.RECIPE_TYPE);
			lasers.setTargetEnergy(requiredLaserEnergy);
			lasers.showResultPanel();

			JEI_MekanismMultiblocks_JeiPlugin.instance().getJeiRuntime().getRecipesGui().showTypes(Arrays.asList(LasersCategory.RECIPE_TYPE));
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
			return 9;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 9;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 9;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 9;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 9;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 9;
		}

		@Override
		public Block getGlassBlock()
		{
			return GeneratorsBlocks.REACTOR_GLASS.get();
		}

	}

}
