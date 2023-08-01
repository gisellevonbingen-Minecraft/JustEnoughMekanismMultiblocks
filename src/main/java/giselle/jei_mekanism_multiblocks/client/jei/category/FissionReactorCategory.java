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
import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class FissionReactorCategory extends MultiblockCategory<FissionReactorCategory.FissionReactorCategoryWidget>
{
	public FissionReactorCategory(IGuiHelper helper)
	{
		super(helper, "fission_reactor", helper.createDrawableIngredient(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getItemStack()));
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

	@Override
	public void setIngredients(FissionReactorCategoryWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends FissionReactorCategoryWidget> getRecipeClass()
	{
		return FissionReactorCategoryWidget.class;
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
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useReactorGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.REACTOR_GLASS.getItemStack().getHoverName()), true));
			this.useReactorGlassCheckBox.addSelectedChangedHandler(this::onUseReactorGlassChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 4, 0));
			this.portsWidget.getSlider().addIntValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addIntValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.burnRateWidget = new LongSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.burn_rate", 0, 0, 0));
			this.burnRateWidget.getSlider().addLongValueChangeHanlder(this::onBurnRateChanged);

			this.updatePortsSliderLimit();
			this.setPortCount(4);
			this.setLogicAdapterCount(0);
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
			int valves = portsSlider.getIntValue();
			portsSlider.setIntMaxValue(this.getSideBlocks());
			portsSlider.setIntValue(valves);

			this.updateLogicAdaptersSliderLimit();
		}

		public void updateLogicAdaptersSliderLimit()
		{
			IntSliderWidget adaptersSlider = this.logicAdaptersWidget.getSlider();
			int adapters = adaptersSlider.getIntValue();
			adaptersSlider.setIntMaxValue(this.getSideBlocks() - this.getPortCount());
			adaptersSlider.setIntValue(adapters);
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
			long burnRate = burnRateSlider.getLongValue();
			burnRateSlider.setLongMaxValue(this.getMaxBurnRate());
			burnRateSlider.setLongValue(burnRate);
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
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			long coolantCapacity = this.getCoolantCapacity();
			long heatedCoolantCapacity = this.getHeatedCoolantCapacity();
			long maxBurnRate = this.getMaxBurnRate();
			long burnRate = this.getBurnRate();
			long fuelCapacity = this.getFuelCapacity();
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.maximum_burn_rate"), VolumeTextHelper.format(maxBurnRate, VolumeUnit.MILLI, "B/t")));
			this.createStableTempWidget(consumer, new FluidStack(Fluids.WATER, 1).getDisplayName(), burnRate, 0.5D);
			this.createStableTempWidget(consumer, MekanismGases.SODIUM.getTextComponent(), burnRate, Coolants.SODIUM_COOLANT.getConductivity());
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_COOLANT_TANK.translate(), VolumeTextHelper.formatMilliBuckets(coolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_FUEL_TANK.translate(), VolumeTextHelper.formatMilliBuckets(fuelCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translate(), VolumeTextHelper.formatMilliBuckets(heatedCoolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_WASTE_TANK.translate(), VolumeTextHelper.formatMilliBuckets(fuelCapacity)));
		}

		private void createStableTempWidget(Consumer<Widget> consumer, ITextComponent with, long toBurn, double conductivity)
		{
			double stableTemp = this.getCoolingStableTemp(toBurn, conductivity);
			ResultWidget tempWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.temp_with", with), MekanismUtils.getTemperatureDisplay(stableTemp, TemperatureUnit.KELVIN, false));
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
				double ratio = MathHelper.inverseLerp(stableTemp, FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE, FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
				int g = (int) MathHelper.clampedLerp(255, 0, ratio);
				tempWidget.getValueLabel().setFGColor(0xFF0000 + g * 256);
			}

			TranslationTextComponent burnRateTooltip = new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.when_burn_rate", VolumeTextHelper.format(toBurn, VolumeUnit.MILLI, "B/t"));

			if (warning)
			{
				tempWidget.getValueLabel().setTooltips(burnRateTooltip, //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.reactor_will_damage").withStyle(TextFormatting.RED));
			}
			else
			{
				tempWidget.getValueLabel().setTooltips(burnRateTooltip);
			}

			long heatedCoolant = this.getHeatedCoolant(stableTemp, conductivity);
			ResultWidget heatedCoolantWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.heated_coolant", with), VolumeTextHelper.format(heatedCoolant, VolumeUnit.MILLI, "B/t"));
			consumer.accept(heatedCoolantWidget);
		}

		private void simulateTemp(double coolantConductivity)
		{
			long coolantCapacity = this.getCoolantCapacity();
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

		public long getHeatedCoolant(double temp, double coolantConductivity)
		{
			double boilEfficiency = 1.0D;
			double boilHeat = boilEfficiency * (temp - HeatUtils.BASE_BOIL_TEMP) * this.getHeatCapacity();
			double caseCoolantHeat = boilHeat * coolantConductivity;
			long coolantHeated = MathUtils.clampToLong(HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy() * caseCoolantHeat);
			return Math.max(0, Math.min(coolantHeated, this.getCoolantCapacity()));
		}

		public long getCoolantCapacity()
		{
			return this.getDimensionVolume() * 100_000L;
		}

		public long getHeatedCoolantCapacity()
		{
			return this.getDimensionVolume() * 1_000_000L;
		}

		public long getMaxBurnRate()
		{
			return this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
		}

		public long getFuelCapacity()
		{
			return this.getFissionFuelAssemblyCount() * 8_000L;
		}

		public double getHeatCapacity()
		{
			return MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * this.getDimensionCasingBlocks();
		}

		public double getCoolingStableTemp(long toBurn, double coolantConductivity)
		{
			long coolantCapacity = this.getCoolantCapacity();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = this.getHeatCapacity();
			double boilEfficiency = 1.0D;

			double coolantHeated = burnHeat / HeatUtils.getWaterThermalEnthalpy() * HeatUtils.getSteamEnergyEfficiency();

			if (coolantHeated > coolantCapacity)
			{
				return Double.POSITIVE_INFINITY;
			}

			double boilHeat = burnHeat / coolantConductivity;
			return boilHeat / (heatCapacity * boilEfficiency) + HeatUtils.BASE_BOIL_TEMP;
		}

		public int getControlRodAssemblyCount()
		{
			Vector3i inner = this.getDimensionInner();
			int rods = 0;
			rods += ((inner.getX() + 1) / 2) * ((inner.getZ() + 1) / 2);
			rods += ((inner.getX() + 0) / 2) * ((inner.getZ() + 0) / 2);
			return rods;
		}

		public int getFissionFuelAssemblyCount()
		{
			Vector3i inner = this.getDimensionInner();
			return this.getControlRodAssemblyCount() * (inner.getY() - 1);
		}

		public int getPortCount()
		{
			return this.portsWidget.getSlider().getIntValue();
		}

		public void setPortCount(int portCount)
		{
			this.portsWidget.getSlider().setIntValue(portCount);
		}

		public int getLogicAdapterCount()
		{
			return this.logicAdaptersWidget.getSlider().getIntValue();
		}

		public void setLogicAdapterCount(int logicAdapterCount)
		{
			this.logicAdaptersWidget.getSlider().setIntValue(logicAdapterCount);
		}

		public boolean isUseReactorGlass()
		{
			return this.useReactorGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useReactorGlass)
		{
			this.useReactorGlassCheckBox.setSelected(useReactorGlass);
		}

		public long getBurnRate()
		{
			return this.burnRateWidget.getSlider().getLongValue();
		}

		public void setBurnRate(long burnRate)
		{
			this.burnRateWidget.getSlider().setLongValue(burnRate);
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
