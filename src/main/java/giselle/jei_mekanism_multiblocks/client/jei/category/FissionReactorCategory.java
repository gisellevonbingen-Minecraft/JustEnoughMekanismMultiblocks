package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.heat.HeatAPI;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3i;
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
		private CheckBoxWidget useReactorGlassCheckBox;
		private IntSliderWithButtons portsWidget;
		private IntSliderWithButtons logicAdaptersWidget;

		public FissionReactorCategoryWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useReactorGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.REACTOR_GLASS.getItemStack().getHoverName()), true, this::onUseReactorGlassChanged));
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 0, 0, this::onPortsChanged));
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0, this::onLogicAdaptersChanged));
			this.updatePortsLimit();
			this.portsWidget.getSlider().setIntValue(4);
			this.logicAdaptersWidget.getSlider().setIntValue(0);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsLimit();
		}

		private void updatePortsLimit()
		{
			IntSliderWidget portsSlider = this.portsWidget.getSlider();
			int valves = portsSlider.getIntValue();
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setIntValue(valves);

			this.updateLogicAdaptersLimit();
		}

		private void updateLogicAdaptersLimit()
		{
			IntSliderWidget adaptersSlider = this.logicAdaptersWidget.getSlider();
			int adapters = adaptersSlider.getIntValue();
			adaptersSlider.setMaxValue(this.getSideBlocks() - this.getPortCount());
			adaptersSlider.setIntValue(adapters);
		}

		protected void onPortsChanged(int ports)
		{
			this.onSliderChanged();
		}

		protected void onLogicAdaptersChanged(int logicAdapters)
		{
			this.onSliderChanged();
		}

		protected void onSliderChanged()
		{
			this.updatePortsLimit();
			this.markNeedUpdateCost();
		}

		protected void onUseReactorGlassChanged(boolean useReactorGlass)
		{
			this.markNeedUpdateCost();
		}

		@Override
		protected void collectCost(Consumer<ItemStack> consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int ports = this.getPortCount();
			int logicAdapter = this.getLogicAdapterCount();
			int options = ports + logicAdapter;

			int blocks = 0;
			int reactorGlasses = 0;

			if (this.isUseReactorGlass())
			{
				blocks = corners;
				reactorGlasses = sides - options;
			}
			else
			{
				blocks = corners + sides - options;
				reactorGlasses = 0;
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_CASING, blocks));
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

			int volume = this.getDimensionVolume();
			long coolantCapacity = volume * 100_000;
			long toBurn = this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
			long fuelCapacity = this.getFissionFuelAssemblyCount() * 8_000;
			long heatedCoolantCapacity = volume * 1_000_000;
			double waterCoolingTemp = this.getCoolingStableTemp(0.5D);
			double sodiumCoolingTemp = this.getCoolingStableTemp(Coolants.SODIUM_COOLANT.getConductivity());
			consumer.accept(new ResultWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.result.maximum_burn_rate"), VolumeTextHelper.format(toBurn, VolumeUnit.MILLI, "B/t")));
			consumer.accept(new ResultWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.result.stable_temp_with", new FluidStack(Fluids.WATER, 1).getDisplayName()), MekanismUtils.getTemperatureDisplay(waterCoolingTemp, TemperatureUnit.KELVIN, false)));
			consumer.accept(new ResultWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.result.stable_temp_with", MekanismGases.SODIUM.getTextComponent()), MekanismUtils.getTemperatureDisplay(sodiumCoolingTemp, TemperatureUnit.KELVIN, false)));
			consumer.accept(new ResultWidget(0, 0, 0, 0, GeneratorsLang.FISSION_COOLANT_TANK.translate(), VolumeTextHelper.formatMilliBuckets(coolantCapacity)));
			consumer.accept(new ResultWidget(0, 0, 0, 0, GeneratorsLang.FISSION_FUEL_TANK.translate(), VolumeTextHelper.formatMilliBuckets(fuelCapacity)));
			consumer.accept(new ResultWidget(0, 0, 0, 0, GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translate(), VolumeTextHelper.formatMilliBuckets(heatedCoolantCapacity)));
			consumer.accept(new ResultWidget(0, 0, 0, 0, GeneratorsLang.FISSION_WASTE_TANK.translate(), VolumeTextHelper.formatMilliBuckets(fuelCapacity)));
		}

		private void simulateTemp(double coolantConductivity)
		{
			int volume = this.getDimensionVolume();
			long coolantCapacity = volume * 100_000;
			long toBurn = this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * (this.getDimensionCornerBlocks() + this.getDimensionSideBlocks());
			double boilEfficiency = 1.0D;

			double heat = HeatAPI.AMBIENT_TEMP * heatCapacity;
			double prevHeat = 0.0D;
			boolean stable = true;

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
					stable = true;
					break;
				}

				prevHeat = heat;
			}

		}

		private double getCoolingStableTemp(double coolantConductivity)
		{
			int volume = this.getDimensionVolume();
			long coolantCapacity = volume * 100_000;
			long toBurn = this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * (this.getDimensionCornerBlocks() + this.getDimensionSideBlocks());
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
