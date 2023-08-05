package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LongSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LongSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.chemical.gas.attribute.GasAttributes.Coolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class FissionReactorCategory extends MultiblockCategory<FissionReactorCategory.FissionReactorCategoryWidget>
{
	public FissionReactorCategory(IGuiHelper helper)
	{
		super(helper, MekanismGenerators.rl("fission_reactor"), FissionReactorCategoryWidget.class, GeneratorsLang.FISSION_REACTOR.translate(), GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_CASING.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_PORT.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY.getItemStack());
		consumer.accept(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getItemStack());
		consumer.accept(GeneratorsBlocks.REACTOR_GLASS.getItemStack());
	}

	public static class FissionReactorCategoryWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useReactorGlassCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected LongSliderWithButtons burnRateWidget;

		public FissionReactorCategoryWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useReactorGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.REACTOR_GLASS.getItemStack().getHoverName()), true));
			this.useReactorGlassCheckBox.addSelectedChangedHandler(this::onUseReactorGlassChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 4, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.burnRateWidget = new LongSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.burn_rate", 0, 0, 0));
			this.burnRateWidget.getSlider().addValueChangeHanlder(this::onBurnRateChanged);

			this.updatePortsSliderLimit();
			this.updateBurnRateSliderLimit();
			this.setBurnRate(this.getMaxBurnRate());
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsSliderLimit();
			this.updateBurnRateSliderLimit();

			this.setBurnRate(this.getMaxBurnRate());
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

		public void updateBurnRateSliderLimit()
		{
			LongSliderWidget burnRateSlider = this.burnRateWidget.getSlider();
			long burnRate = burnRateSlider.getValue();
			burnRateSlider.setMaxValue(this.getMaxBurnRate());
			burnRateSlider.setValue(burnRate);
		}

		protected void onBurnRateChanged(long burnRate)
		{
			this.markNeedUpdate();
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

			int casings = 0;
			int reactorGlasses = 0;

			if (this.isUseReactorGlass())
			{
				casings = corners;
				reactorGlasses = sides;
			}
			else
			{
				casings = corners + sides;
				reactorGlasses = 0;
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_CASING, casings));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, this.getFissionFuelAssemblyCount()));
			consumer.accept(new ItemStack(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY, this.getControlRodAssemblyCount()));
			consumer.accept(new ItemStack(GeneratorsBlocks.REACTOR_GLASS, reactorGlasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			long coolantCapacity = this.getCooledCoolantCapacity();
			long heatedCoolantCapacity = this.getHeatedCoolantCapacity();
			long maxBurnRate = this.getMaxBurnRate();
			long burnRate = this.getBurnRate();
			long fuelCapacity = this.getFuelCapacity();
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.max_burn_rate"), VolumeTextHelper.formatMBt(maxBurnRate)));
			this.createStableTempWidget(consumer, new FluidStack(Fluids.WATER, 1).getDisplayName(), burnRate, 0.5D, HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
			this.createStableTempWidget(consumer, MekanismGases.SODIUM.getTextComponent(), burnRate, Coolants.SODIUM_COOLANT);
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(coolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_FUEL_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(heatedCoolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_WASTE_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
		}

		private void createStableTempWidget(Consumer<AbstractWidget> consumer, Component with, long toBurn, Coolant coolant)
		{
			this.createStableTempWidget(consumer, with, toBurn, coolant.getConductivity(), coolant.getThermalEnthalpy());
		}

		private void createStableTempWidget(Consumer<AbstractWidget> consumer, Component with, long toBurn, double conductivity, double thermalEnthalpy)
		{
			double stableTemp = this.getCoolingStableTemp(toBurn, conductivity, thermalEnthalpy);
			ResultWidget tempWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.temp_with", with), MekanismUtils.getTemperatureDisplay(stableTemp, TemperatureUnit.KELVIN, false));
			consumer.accept(tempWidget);

			boolean warning = false;

			if (Double.isInfinite(stableTemp))
			{
				warning = true;
				tempWidget.getValueLabel().setFGColor(0xFF0000);
			}
			else if (stableTemp >= FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE)
			{
				warning = true;
				double ratio = Mth.inverseLerp(stableTemp, FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE, FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
				int g = (int) Mth.clampedLerp(255, 0, ratio);
				tempWidget.getValueLabel().setFGColor(0xFF0000 + g * 256);
			}

			Component burnRateTooltip = Component.translatable("text.jei_mekanism_multiblocks.tooltip.when_burn_rate", VolumeTextHelper.formatMBt(toBurn));

			if (warning)
			{
				tempWidget.setTooltip(burnRateTooltip, //
						Component.translatable("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(ChatFormatting.RED), //
						Component.translatable("text.jei_mekanism_multiblocks.tooltip.reactor_will_damage").withStyle(ChatFormatting.RED));
			}
			else
			{
				tempWidget.setTooltip(burnRateTooltip);
			}

			long heatedCoolant = this.getHeatedCoolant(stableTemp, conductivity, thermalEnthalpy);
			ResultWidget heatingRateWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.heating_rate_with", with), VolumeTextHelper.formatMBt(heatedCoolant));
			heatingRateWidget.setTooltip(burnRateTooltip);
			consumer.accept(heatingRateWidget);
		}

		private void simulateTemp(double coolantConductivity)
		{
			long coolantCapacity = this.getCooledCoolantCapacity();
			long toBurn = this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = this.getHeatCapacity();
			double boilEfficiency = 1.0D;

			double heat = HeatAPI.AMBIENT_TEMP * heatCapacity;
			double prevHeat = 0.0D;

			for (int i = 0; i < 100; i++)
			{
				double temp = heat / heatCapacity;

				heat += burnHeat;

				double boilHeat = boilEfficiency * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacity;
				double caseCoolantHeat = boilHeat * coolantConductivity;
				long coolantHeated = (int) (HeatUtils.getSteamEnergyEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy());
				coolantHeated = Math.max(0, Math.min(coolantHeated, coolantCapacity));

				if (coolantHeated > 0)
				{
					caseCoolantHeat = coolantHeated * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
					heat -= caseCoolantHeat;
				}

				System.out.println("Temp: " + (heat / heatCapacity));

				if (prevHeat == heat)
				{
					System.out.println("Stabled");
					break;
				}

				prevHeat = heat;
			}

		}

		public long getHeatedCoolant(double temp, double coolantConductivity, double thermalEnthalpy)
		{
			double boilEfficiency = 1.0D;
			double boilHeat = boilEfficiency * (temp - HeatUtils.BASE_BOIL_TEMP) * this.getHeatCapacity();
			double caseCoolantHeat = boilHeat * coolantConductivity;
			long coolantHeated = MathUtils.clampToLong(caseCoolantHeat / thermalEnthalpy);
			return Math.max(0, Math.min(coolantHeated, this.getCooledCoolantCapacity()));
		}

		public long getCooledCoolantCapacity()
		{
			return this.getDimensionVolume() * MekanismGeneratorsConfig.generators.fissionCooledCoolantPerTank.get();
		}

		public long getHeatedCoolantCapacity()
		{
			return this.getDimensionVolume() * MekanismGeneratorsConfig.generators.fissionHeatedCoolantPerTank.get();
		}

		public long getMaxBurnRate()
		{
			return this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
		}

		public long getFuelCapacity()
		{
			return this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.maxFuelPerAssembly.get();
		}

		public double getHeatCapacity()
		{
			return MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * this.getDimensionCasingBlocks();
		}

		public double getCoolingStableTemp(long toBurn, double coolantConductivity, double thermalEnthalpy)
		{
			long coolantCapacity = this.getCooledCoolantCapacity();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = this.getHeatCapacity();
			double boilEfficiency = 1.0D;

			double coolantHeated = burnHeat / thermalEnthalpy;

			if (coolantHeated > coolantCapacity)
			{
				return Double.POSITIVE_INFINITY;
			}

			double boilHeat = burnHeat / coolantConductivity;
			return boilHeat / (heatCapacity * boilEfficiency) + HeatUtils.BASE_BOIL_TEMP;
		}

		public int getControlRodAssemblyCount()
		{
			Vec3i inner = this.getDimensionInner();
			int rods = 0;
			rods += ((inner.getX() + 1) / 2) * ((inner.getZ() + 1) / 2);
			rods += ((inner.getX() + 0) / 2) * ((inner.getZ() + 0) / 2);
			return rods;
		}

		public int getFissionFuelAssemblyCount()
		{
			Vec3i inner = this.getDimensionInner();
			return this.getControlRodAssemblyCount() * (inner.getY() - 1);
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

		public void setUseReactorGlass(boolean useReactorGlass)
		{
			this.useReactorGlassCheckBox.setSelected(useReactorGlass);
		}

		public long getBurnRate()
		{
			return this.burnRateWidget.getSlider().getValue();
		}

		public void setBurnRate(long burnRate)
		{
			this.burnRateWidget.getSlider().setValue(burnRate);
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 3;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 18;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 3;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 18;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 4;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

	}

}
