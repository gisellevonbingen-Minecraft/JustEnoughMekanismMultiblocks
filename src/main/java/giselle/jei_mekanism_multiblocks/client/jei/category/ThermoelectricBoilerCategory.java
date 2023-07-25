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
import net.minecraft.util.text.ITextComponent;
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
		private IntSliderWithButtons heatingElementsWidget;

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true, this::onUseStructuralGlassChanged));

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 0, 0, this::onValvesChanged));
			consumer.accept(this.steamHeightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.steam_height", 0, 1, 0, this::onSteamHeightChanged));
			consumer.accept(this.heatingElementsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.heating_elements", 0, 1, 0, this::onHeatingElementsChanged));
			this.updateSlidersLimit();
			this.setValveCount(2);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateSlidersLimit();

			IntSliderWidget steamHeightSlider = this.steamHeightWidget.getSlider();
			IntSliderWidget heatingElementsSlider = this.heatingElementsWidget.getSlider();
			double maxBoil = 0.0D;
			int preferredSteamHeight = 0;
			int preferredHeatingElementCount = 0;

			// long nano = System.nanoTime();
			int steamHeight = steamHeightSlider.getMinValue();
			int heatingElementCount = heatingElementsSlider.getMinValue();

			while (true)
			{
				if (steamHeight > steamHeightSlider.getMaxValue() || heatingElementCount > heatingElementsSlider.getMaxValue())
				{
					break;
				}

				ThermoelectricBoilerSimulation simulation = this.simulateMaxBoil(steamHeight, heatingElementCount);

				if (simulation.maxBoil > maxBoil)
				{
					maxBoil = simulation.maxBoil;
					preferredSteamHeight = steamHeight;
					preferredHeatingElementCount = heatingElementCount;
				}

				if (simulation.needMoreSuperHeatingElemetns)
				{
					heatingElementCount++;
				}
				else if (simulation.needMoreSteamVolume)
				{
					steamHeight++;
				}
				else
				{
					break;
				}

			}

			// System.out.println("Simulation for " + (System.nanoTime() - nano) / 1000_000D + "ms");

			if (preferredSteamHeight > 0)
			{
				this.setSteamHeight(preferredSteamHeight);
				this.SetHeatingElementCount(preferredHeatingElementCount);
			}

		}

		public void updateSlidersLimit()
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

		public void updateHeatingHeightSliderLimit()
		{
			Vector3i inner = this.getDimensionInner();
			IntSliderWidget heatingElementsSlider = this.heatingElementsWidget.getSlider();
			int heatingElements = heatingElementsSlider.getIntValue();
			heatingElementsSlider.setMaxValue((this.getInnerAdjustableHeight() - this.getSteamHeight() + 2) * (inner.getX() * inner.getZ()));
			heatingElementsSlider.setIntValue(heatingElements);
		}

		protected void onHeatingElementsChanged(int elements)
		{
			this.markNeedUpdateCost();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdateCost();
		}

		@Override
		public void collectCost(Consumer<ItemStack> consumer)
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

			consumer.accept(new ItemStack(MekanismBlocks.PRESSURE_DISPERSER, this.getPressureDispenserCount()));
			consumer.accept(new ItemStack(MekanismBlocks.SUPERHEATING_ELEMENT, this.getHeatingElementCount()));
		}

		@Override
		public void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int steamHeight = this.getSteamHeight();
			int heatingElementCount = this.getHeatingElementCount();
			ThermoelectricBoilerSimulation simulation = this.simulateMaxBoil(steamHeight, heatingElementCount);
			// System.out.println("needMoreSteamVolume: " + simulation.needMoreSteamVolume);
			// System.out.println("needMoreSuperHeatingElemetns: " + simulation.needMoreSuperHeatingElemetns);

			ResultWidget boilRateWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_boil_rate"), VolumeTextHelper.format(simulation.maxBoil, VolumeUnit.MILLI, "B/t"));

			if (simulation.needMoreSuperHeatingElemetns)
			{
				IntSliderWidget heatingElementsSlider = this.heatingElementsWidget.getSlider();
				int testHeatingElementCount = Math.min(heatingElementCount + 1, heatingElementsSlider.getMaxValue());
				ThermoelectricBoilerSimulation simulation2 = this.simulateMaxBoil(steamHeight, testHeatingElementCount);

				if (simulation2.maxBoil > simulation.maxBoil)
				{
					boilRateWidget.getValueLabel().setFGColor(0xFFFF00);
					boilRateWidget.getValueLabel().setTooltips(new ITextComponent[]{new TranslationTextComponent("text.jei_mekanism_multiblocks.toolip.limited"), new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_boil_rate.need_more_heating_elements")});
				}

			}

			consumer.accept(boilRateWidget);
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.temp"), MekanismUtils.getTemperatureDisplay(simulation.heat / simulation.heatCapacity, TemperatureUnit.KELVIN, false)));

			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMilliBuckets(simulation.waterTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMilliBuckets(simulation.steamTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.heated_coolant_tank"), VolumeTextHelper.formatMilliBuckets(simulation.heatedCoolantTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.cooled_coolant_tank"), VolumeTextHelper.formatMilliBuckets(simulation.cooledCoolantTank)));
		}

		public ThermoelectricBoilerSimulation simulateMaxBoil(int steamHeight, int heatingElementCount)
		{
			int steamVolume = this.getSteamVolume(steamHeight);
			int waterVolume = this.getWaterVolume(steamHeight, heatingElementCount);

			ThermoelectricBoilerSimulation simulation = new ThermoelectricBoilerSimulation();
			simulation.heatCapacity = this.getHeatCapacity();
			simulation.steamTank = this.getSteamTank(steamVolume);
			simulation.heatedCoolantTank = this.getHeatedCoolantTank(waterVolume);
			simulation.waterTank = this.getWaterTank(waterVolume);
			simulation.cooledCoolantTank = this.getCooledCoolantTank(steamVolume);
			simulation.heatingCapacity = this.getHeatingCapacity(heatingElementCount);
			simulation.cycleForStable();

			return simulation;
		}

		public double getHeatingCapacity(int superHeatingElementCount)
		{
			return MekanismConfig.general.superheatingHeatTransfer.get() * superHeatingElementCount;
		}

		public double getHeatCapacity()
		{
			return BoilerMultiblockData.CASING_HEAT_CAPACITY * this.getDimensionCasingBlocks();
		}

		public int getWaterVolume(int steamHeight, int heatingElenmentCount)
		{
			Vector3i outer = this.getDimension();
			int outerSquare = outer.getX() * outer.getZ();
			Vector3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			return (outerSquare * inner.getY()) - (this.getSteamVolume(steamHeight) - outerSquare + innerSquare) - (heatingElenmentCount - innerSquare);
		}

		public int getSteamVolume(int steamHeight)
		{
			Vector3i outer = this.getDimension();
			return outer.getX() * outer.getZ() * steamHeight;
		}

		public long getWaterTank(int waterVolume)
		{
			return waterVolume * 16_000L;
		}

		public long getSteamTank(int steamVolume)
		{
			return steamVolume * 160_000L;
		}

		public long getHeatedCoolantTank(int steamVolume)
		{
			return steamVolume * 256_000L;
		}

		public long getCooledCoolantTank(int waterVolume)
		{
			return waterVolume * 256_000L;
		}

		public int getPressureDispenserCount()
		{
			Vector3i inner = this.getDimensionInner();
			return inner.getX() * inner.getZ();
		}

		public int getInnerAdjustableHeight()
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

		public int getHeatingElementCount()
		{
			return this.heatingElementsWidget.getSlider().getIntValue();
		}

		public void SetHeatingElementCount(int heatingElementCount)
		{
			this.heatingElementsWidget.getSlider().setIntValue(heatingElementCount);
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

	public static class ThermoelectricBoilerSimulation
	{
		public double heatCapacity;
		public long heatedCoolantTank;
		public long cooledCoolantTank;
		public long waterTank;
		public long steamTank;
		public double heatingCapacity;
		public double coolantCoolingEfficiency;
		public double coolantThermalEnthalpy;

		public double heat;
		public double thermalHeat;
		public double boilHeat;
		public double heatOnTemp;
		public boolean needMoreSteamVolume;
		public boolean needMoreSuperHeatingElemetns;
		public long maxBoil;

		public ThermoelectricBoilerSimulation()
		{
			this.coolantCoolingEfficiency = 0.4D;
			this.coolantThermalEnthalpy = Coolants.HEATED_SODIUM_COOLANT.getThermalEnthalpy();
		}

		public void reset()
		{
			this.heat = HeatAPI.AMBIENT_TEMP * this.heatCapacity;
		}

		public void cycleForStable()
		{
			this.reset();

			long prevMaxBoil = -1;

			while (true)
			{
				this.tick();

				if (prevMaxBoil > -1 && prevMaxBoil >= this.maxBoil)
				{
					break;
				}

				prevMaxBoil = this.maxBoil;
			}

		}

		public void tick()
		{
			double temp = this.heat / heatCapacity;

			long toCool = Math.round(this.heatedCoolantTank * this.coolantCoolingEfficiency);
			toCool = MathUtils.clampToLong(toCool * (1.0D - (temp / HeatUtils.HEATED_COOLANT_TEMP)));

			if (toCool > this.cooledCoolantTank)
			{
				toCool = this.cooledCoolantTank;
				this.needMoreSteamVolume = true;
			}
			else
			{
				this.needMoreSteamVolume = false;
			}

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
			else
			{
				this.needMoreSuperHeatingElemetns = false;
			}

			this.maxBoil = (int) Math.floor(HeatUtils.getSteamEnergyEfficiency() * boilingHeat / HeatUtils.getWaterThermalEnthalpy());
			this.maxBoil = Math.min(this.maxBoil, Math.min(this.waterTank, this.steamTank));
			this.boilHeat = this.maxBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();

			if (this.boilHeat > 0)
			{
				this.heat -= this.boilHeat;
			}

		}

	}

}
