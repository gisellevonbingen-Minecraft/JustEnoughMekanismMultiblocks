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
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TranslationTextComponent;

public class ThermoelectricBoilerCategory extends MultiblockCategory<ThermoelectricBoilerCategory.ThermalBoilerWidget>
{
	public ThermoelectricBoilerCategory(IGuiHelper helper)
	{
		super(helper, "thermoelectric_boiler", null);
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.BOILER_CASING.getItemStack());
		consumer.accept(MekanismBlocks.BOILER_VALVE.getItemStack());
		consumer.accept(MekanismBlocks.PRESSURE_DISPERSER.getItemStack());
		consumer.accept(MekanismBlocks.SUPERHEATING_ELEMENT.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());
	}

	@Override
	public void setIngredients(ThermalBoilerWidget recipe, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends ThermalBoilerWidget> getRecipeClass()
	{
		return ThermalBoilerWidget.class;
	}

	public static class ThermalBoilerWidget extends MultiblockWidget
	{
		private CheckBoxWidget useStructuralGlassCheckBox;
		private IntSliderWithButtons valvesWidget;
		private IntSliderWithButtons steamHeightWidget;
		private IntSliderWithButtons heatingHeightWidget;

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true, this::onUseStructuralGlassChanged));

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 0, 0, this::onValvesChanged));
			consumer.accept(this.steamHeightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.steam_height", 0, 1, 0, this::onSteamHeightChanged));
			consumer.accept(this.heatingHeightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.heating_height", 0, 1, 0, this::onHeatingHeightChanged));
			this.updateSlidersLimit();
			this.setValveCount(2);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateSlidersLimit();

			IntSliderWidget steamHeightSlider = this.steamHeightWidget.getSlider();
			IntSliderWidget heatingHeightSlider = this.heatingHeightWidget.getSlider();
			double maxBoil = 0.0D;
			int preferredSteamHeight = 0;
			int preferredHeatingHeight = 0;

			long millis = System.currentTimeMillis();

			for (int steamHeight = steamHeightSlider.getMinValue(); steamHeight <= steamHeightSlider.getMaxValue(); steamHeight++)
			{
				for (int heatingHeight = heatingHeightSlider.getMinValue(); heatingHeight <= heatingHeightSlider.getMaxValue(); heatingHeight++)
				{
					int superHeatingElements = this.getSuperHeatingElements(heatingHeight);
					int steamVolume = this.getSteamVolume(steamHeight);
					int waterVolume = this.getWaterVolume(steamHeight, heatingHeight);

					MaxBoilSimulation simulation = new MaxBoilSimulation();
					simulation.heatCapacity = this.getHeatCapacity();
					simulation.steamTank = this.getSteamTank(steamVolume);
					simulation.heatedCoolantTank = this.getHeatedCoolantTank(waterVolume);
					simulation.waterTank = this.getWaterTank(waterVolume);
					simulation.cooledCoolantTank = this.getCooledCoolantTank(steamVolume);
					simulation.heatingCapacity = this.getHeatingCapacity(superHeatingElements);
					simulation.simulate();

					if (simulation.maxBoil > maxBoil)
					{
						maxBoil = simulation.maxBoil;
						preferredSteamHeight = steamHeight;
						preferredHeatingHeight = heatingHeight;
					}
					else if (simulation.needMoreSuperHeatingElemetns == false)
					{
						break;
					}

				}

			}

			System.out.println("Simulation for " + (System.currentTimeMillis() - millis) + "ms");

			if (preferredSteamHeight > 0)
			{
				this.setSteamHeight(preferredSteamHeight);
				this.SetHeatingHeight(preferredHeatingHeight);
			}

		}

		private void updateSlidersLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getIntValue();
			valvesSlider.setMaxValue(this.getSideBlocks());
			valvesSlider.setIntValue(valves);

			IntSliderWidget steamHeightSlider = this.steamHeightWidget.getSlider();
			int steamHeight = steamHeightSlider.getIntValue();
			steamHeightSlider.setMaxValue(this.getInnerAdjustableHeight() + 1);
			steamHeightSlider.setIntValue(steamHeight);

			this.updateHeatingHeightSliderLimit();
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdateCost();
		}

		protected void onSteamHeightChanged(int height)
		{
			this.updateHeatingHeightSliderLimit();
			this.markNeedUpdateCost();
		}

		private void updateHeatingHeightSliderLimit()
		{
			IntSliderWidget heatingHeightSlider = this.heatingHeightWidget.getSlider();
			int heatingHeight = heatingHeightSlider.getIntValue();
			heatingHeightSlider.setMaxValue(this.getInnerAdjustableHeight() - this.getSteamHeight() + 2);
			heatingHeightSlider.setIntValue(heatingHeight);
		}

		protected void onHeatingHeightChanged(int height)
		{
			this.markNeedUpdateCost();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdateCost();
		}

		@Override
		protected void collectCost(Consumer<ItemStack> consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int valves = this.getValveCount();

			int casing = 0;
			int structuralGlasses = 0;

			if (this.isUseStruturalGlass())
			{
				casing = corners;
				structuralGlasses = sides - valves;
			}
			else
			{
				casing = corners + sides - valves;
			}

			consumer.accept(new ItemStack(MekanismBlocks.BOILER_CASING, casing));
			consumer.accept(new ItemStack(MekanismBlocks.BOILER_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));

			consumer.accept(new ItemStack(MekanismBlocks.PRESSURE_DISPERSER, this.getPressureDispensers()));
			consumer.accept(new ItemStack(MekanismBlocks.SUPERHEATING_ELEMENT, this.getSuperHeatingElements()));
		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int steamHeight = this.getSteamHeight();
			int heatingHeight = this.getHeatingHeight();
			int superHeatingElements = this.getSuperHeatingElements(heatingHeight);
			int steamVolume = this.getSteamVolume(steamHeight);
			int waterVolume = this.getWaterVolume(steamHeight, heatingHeight);

			long steamTank = this.getSteamTank(steamVolume);
			long heatedCoolantTank = this.getHeatedCoolantTank(waterVolume);
			long waterTank = this.getWaterTank(waterVolume);
			long cooledCoolantTank = this.getCooledCoolantTank(steamVolume);

			MaxBoilSimulation simulation = new MaxBoilSimulation();
			simulation.heatCapacity = this.getHeatCapacity();
			simulation.steamTank = steamTank;
			simulation.heatedCoolantTank = heatedCoolantTank;
			simulation.waterTank = waterTank;
			simulation.cooledCoolantTank = cooledCoolantTank;
			simulation.heatingCapacity = this.getHeatingCapacity(superHeatingElements);
			simulation.simulate();

			double temp = simulation.heat / simulation.heatCapacity;
			long maxBoil = simulation.maxBoil;

			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.temp"), MekanismUtils.getTemperatureDisplay(temp, TemperatureUnit.KELVIN, false)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_boil_rate"), VolumeTextHelper.format(maxBoil, VolumeUnit.MILLI, "B/t")));
		
			
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMilliBuckets(waterTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMilliBuckets(steamTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.heated_coolant_tank"), VolumeTextHelper.formatMilliBuckets(heatedCoolantTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.cooled_coolant_tank"), VolumeTextHelper.formatMilliBuckets(cooledCoolantTank)));
		}

		private class MaxBoilSimulation
		{
			private double heatCapacity;
			private long heatedCoolantTank;
			private long cooledCoolantTank;
			private long waterTank;
			private long steamTank;
			private double heatingCapacity;
			private double coolantCoolingEfficiency;
			private double coolantThermalEnthalpy;

			private double heat;
			private double thermalHeat;
			private double boilHeat;
			private double heatOnTemp;
			private boolean needMoreSuperHeatingElemetns;
			private long maxBoil;

			public MaxBoilSimulation()
			{
				this.coolantCoolingEfficiency = 0.4D;
				this.coolantThermalEnthalpy = Coolants.HEATED_SODIUM_COOLANT.getThermalEnthalpy();
			}

			public void simulate()
			{
				double heatCapacity = this.heatCapacity;
				this.heat = HeatAPI.AMBIENT_TEMP * heatCapacity;
				long prevMaxBoil = -1;

				while (true)
				{
					double temp = this.heat / heatCapacity;

					long toCool = Math.round(this.heatedCoolantTank * this.coolantCoolingEfficiency);
					toCool = MathUtils.clampToLong(toCool * (1.0D - (temp / HeatUtils.HEATED_COOLANT_TEMP)));
					toCool = Math.min(toCool, this.cooledCoolantTank);
					this.thermalHeat = Math.max(toCool * this.coolantThermalEnthalpy, 0.0D);

					if (this.thermalHeat > 0.0D)
					{
						this.heat += this.thermalHeat;
					}

					this.heatOnTemp = (temp - HeatUtils.BASE_BOIL_TEMP) * (heatCapacity * MekanismConfig.general.boilerWaterConductivity.get());
					double boilingHeat = this.heatOnTemp;

					if (this.heatOnTemp > this.heatingCapacity)
					{
						boilingHeat = this.heatingCapacity;
						this.needMoreSuperHeatingElemetns = true;
					}

					this.maxBoil = (int) Math.floor(HeatUtils.getSteamEnergyEfficiency() * boilingHeat / HeatUtils.getWaterThermalEnthalpy());
					this.maxBoil = Math.min(this.maxBoil, Math.min(this.waterTank, this.steamTank));
					this.boilHeat = this.maxBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();

					if (this.boilHeat > 0)
					{
						this.heat -= this.boilHeat;
					}

					if (prevMaxBoil > -1 && prevMaxBoil >= this.maxBoil)
					{
						break;
					}

					prevMaxBoil = this.maxBoil;
				}

			}

		}

		private double getHeatingCapacity(int superHeatingElements)
		{
			return MekanismConfig.general.superheatingHeatTransfer.get() * superHeatingElements;
		}

		private double getHeatCapacity()
		{
			return BoilerMultiblockData.CASING_HEAT_CAPACITY * this.getDimensionCasingBlocks();
		}

		private int getWaterVolume(int steamHeight, int heatingHeight)
		{
			Vector3i outer = this.getDimension();
			int outerSquare = outer.getX() * outer.getZ();
			Vector3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			return (outerSquare * inner.getY()) - (this.getSteamVolume(steamHeight) - outerSquare + innerSquare) - (this.getSuperHeatingElements(heatingHeight) - innerSquare);
		}

		private int getSteamVolume(int steamHeight)
		{
			Vector3i outer = this.getDimension();
			return outer.getX() * outer.getZ() * steamHeight;
		}

		private long getWaterTank(int waterVolume)
		{
			return waterVolume * 16_000L;
		}

		private long getSteamTank(int steamVolume)
		{
			return steamVolume * 160_000L;
		}

		private long getHeatedCoolantTank(int steamVolume)
		{
			return steamVolume * 256_000L;
		}

		private long getCooledCoolantTank(int waterVolume)
		{
			return waterVolume * 256_000L;
		}

		private int getPressureDispensers()
		{
			Vector3i inner = this.getDimensionInner();
			return inner.getX() * inner.getZ();
		}

		private int getSuperHeatingElements()
		{
			return this.getSuperHeatingElements(this.getHeatingHeight());
		}

		private int getSuperHeatingElements(int heatingHeight)
		{
			Vector3i inner = this.getDimensionInner();
			return inner.getX() * inner.getZ() * heatingHeight;
		}

		private int getInnerAdjustableHeight()
		{
			return this.getDimensionInner().getY() - 2;
		}

		public int getValveCount()
		{
			return this.valvesWidget.getSlider().getIntValue();
		}

		public void setValveCount(int valveCount)
		{
			this.valvesWidget.getSlider().setIntValue(valveCount);
		}

		public boolean isUseStruturalGlass()
		{
			return this.useStructuralGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useStructuralGlass)
		{
			this.useStructuralGlassCheckBox.setSelected(useStructuralGlass);
		}

		public int getSteamHeight()
		{
			return this.steamHeightWidget.getSlider().getIntValue();
		}

		public void setSteamHeight(int height)
		{
			this.steamHeightWidget.getSlider().setIntValue(height);
		}

		public int getHeatingHeight()
		{
			return this.heatingHeightWidget.getSlider().getIntValue();
		}

		public void SetHeatingHeight(int height)
		{
			this.heatingHeightWidget.getSlider().setIntValue(height);
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
