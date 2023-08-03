package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.CostWidget;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.HeatUtils;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class BoilerCategory extends MultiblockCategory<BoilerCategory.BoilerWidget>
{
	public BoilerCategory(IGuiHelper helper)
	{
		super(helper, Mekanism.rl("boiler"), MekanismLang.BOILER.translate(), MekanismBlocks.BOILER_VALVE.getItemStack());
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
	public void setIngredients(BoilerWidget recipe, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends BoilerWidget> getRecipeClass()
	{
		return BoilerWidget.class;
	}

	public static class BoilerWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected CheckBoxWidget forSodiumCoolingCheckBox;
		protected IntSliderWithButtons valvesWidget;
		protected IntSliderWithButtons steamHeightWidget;
		protected IntSliderWithButtons heatingElementsWidget;

		private boolean needMoreHeatingElements;

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useStructuralGlassCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			consumer.accept(this.forSodiumCoolingCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.for_sodium_cooling"), false));
			this.forSodiumCoolingCheckBox.addSelectedChangedHandler(this::onForSodiumCoolingChanged);

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
			this.valvesWidget.getSlider().addValueChangeHanlder(this::onValvesChanged);
			consumer.accept(this.steamHeightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.steam_height", 0, 1, 0));
			this.steamHeightWidget.getSlider().addValueChangeHanlder(this::onSteamHeightChanged);
			this.steamHeightWidget.setTooltip(new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.steam_height", MekanismBlocks.PRESSURE_DISPERSER.getTextComponent()));
			consumer.accept(this.heatingElementsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.heating_elements", 0, 1, 0));
			this.heatingElementsWidget.getSlider().addValueChangeHanlder(this::onHeatingElementsChanged);

			this.updateSlidersLimit();
			this.setValveCount(2);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateSlidersLimit();

			if (this.isForSodiumCooling())
			{
				this.calculatePreferredSodiumCoolingLayout();
			}

		}

		public void calculatePreferredSodiumCoolingLayout()
		{
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
				if (steamHeight > steamHeightSlider.getMaxValue() || heatingElementCount > this.getMaxHeatingElements(steamHeight))
				{
					break;
				}

				BoilerCoolingSimulation simulation = this.simulateSodiumCooling(steamHeight, heatingElementCount);

				if (simulation.maxBoil > maxBoil)
				{
					maxBoil = simulation.maxBoil;
					preferredSteamHeight = steamHeight;
					preferredHeatingElementCount = heatingElementCount;
				}

				if (simulation.needMoreSuperHeatingElements)
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
			int valves = valvesSlider.getValue();
			valvesSlider.setMaxValue(this.getSideBlocks());
			valvesSlider.setValue(valves);

			IntSliderWidget steamHeightSlider = this.steamHeightWidget.getSlider();
			int steamHeight = steamHeightSlider.getValue();
			steamHeightSlider.setMaxValue(this.getInnerAdjustableHeight() + steamHeightSlider.getMinValue());
			steamHeightSlider.setValue(steamHeight);

			this.updateHeatingHeightSliderLimit();
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdate();
		}

		protected void onSteamHeightChanged(int height)
		{
			this.updateHeatingHeightSliderLimit();
			this.markNeedUpdate();
		}

		public void updateHeatingHeightSliderLimit()
		{
			IntSliderWidget heatingElementsSlider = this.heatingElementsWidget.getSlider();
			int heatingElements = heatingElementsSlider.getValue();
			heatingElementsSlider.setMaxValue(this.getMaxHeatingElements(this.getSteamHeight()));
			heatingElementsSlider.setValue(heatingElements);
		}

		public int getMaxHeatingElements(int steamHeight)
		{
			Vector3i inner = this.getDimensionInner();
			return (this.getInnerAdjustableHeight() - steamHeight + 2) * (inner.getX() * inner.getZ());
		}

		protected void onHeatingElementsChanged(int elements)
		{
			this.markNeedUpdate();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdate();
		}

		protected void onForSodiumCoolingChanged(boolean forSodiumCooling)
		{
			if (forSodiumCooling)
			{
				this.calculatePreferredSodiumCoolingLayout();
			}

			this.markNeedUpdate();
		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int valves = this.getValveCount();
			sides -= valves;

			int casing = 0;
			int structuralGlasses = 0;

			if (this.isUseStruturalGlass())
			{
				casing = corners;
				structuralGlasses = sides;
			}
			else
			{
				casing = corners + sides;
			}

			consumer.accept(new ItemStack(MekanismBlocks.BOILER_CASING, casing));
			consumer.accept(new ItemStack(MekanismBlocks.BOILER_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));

			consumer.accept(new ItemStack(MekanismBlocks.PRESSURE_DISPERSER, this.getPressureDispenserCount()));
			CostWidget heatingElements = consumer.accept(new ItemStack(MekanismBlocks.SUPERHEATING_ELEMENT, this.getHeatingElementCount()));

			if (this.needMoreHeatingElements)
			{
				heatingElements.setFGColor(0xFF8000);
				heatingElements.setHeadTooltip(//
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.value_limited", new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_boil_rate")).withStyle(TextFormatting.RED), new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", MekanismBlocks.SUPERHEATING_ELEMENT.getTextComponent()).withStyle(TextFormatting.RED));
			}

		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int steamHeight = this.getSteamHeight();
			int heatingElementCount = this.getHeatingElementCount();

			if (this.isForSodiumCooling())
			{
				BoilerCoolingSimulation simulation = this.simulateSodiumCooling(steamHeight, heatingElementCount);
				// System.out.println("needMoreSteamVolume: " + simulation.needMoreSteamVolume);
				// System.out.println("needMoreSuperHeatingElemetns: " + simulation.needMoreSuperHeatingElemetns);

				ResultWidget boilRateWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.boil_rate"), VolumeTextHelper.formatMBt(simulation.maxBoil));
				this.needMoreHeatingElements = false;

				if (simulation.needMoreSuperHeatingElements)
				{
					IntSliderWidget heatingElementsSlider = this.heatingElementsWidget.getSlider();
					int testHeatingElementCount = Math.min(heatingElementCount + 1, heatingElementsSlider.getMaxValue());
					BoilerCoolingSimulation simulation2 = this.simulateSodiumCooling(steamHeight, testHeatingElementCount);

					if (simulation2.maxBoil > simulation.maxBoil)
					{
						this.needMoreHeatingElements = true;
						boilRateWidget.getValueLabel().setFGColor(0xFF8000);
						boilRateWidget.setTooltip(//
								new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.limited").withStyle(TextFormatting.RED), //
								new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", MekanismBlocks.SUPERHEATING_ELEMENT.getTextComponent()).withStyle(TextFormatting.RED));
					}

				}

				consumer.accept(boilRateWidget);
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMB(simulation.waterTank)));
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(simulation.steamTank)));
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.heated_coolant_tank"), VolumeTextHelper.formatMB(simulation.heatedCoolantTank)));
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.cooled_coolant_tank"), VolumeTextHelper.formatMB(simulation.cooledCoolantTank)));
			}
			else
			{
				int steamVolume = this.getSteamVolume(steamHeight);
				int waterVolume = this.getWaterVolume(steamHeight, heatingElementCount);

				double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * this.getHeatingElementCount() / HeatUtils.getWaterThermalEnthalpy();
				boilCapacity = MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());

				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.boil_capacity"), VolumeTextHelper.formatMBt(boilCapacity)));
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMB(this.getWaterTank(waterVolume))));
				consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(this.getSteamTank(steamVolume))));
			}

		}

		public BoilerCoolingSimulation simulateSodiumCooling(int steamHeight, int heatingElementCount)
		{
			BoilerCoolingSimulation simulation = this.createSodiumCoolingSimulation(steamHeight, heatingElementCount, 0.4D, Coolants.HEATED_SODIUM_COOLANT.getThermalEnthalpy());
			simulation.cycleForStableBoil();

			return simulation;
		}

		public BoilerCoolingSimulation createSodiumCoolingSimulation(int steamHeight, int heatingElementCount, double coolantCoolingEfficiency, double coolantThermalEnthalpy)
		{
			int steamVolume = this.getSteamVolume(steamHeight);
			int waterVolume = this.getWaterVolume(steamHeight, heatingElementCount);

			BoilerCoolingSimulation simulation = new BoilerCoolingSimulation();
			simulation.heatCapacity = this.getHeatCapacity();
			simulation.steamTank = this.getSteamTank(steamVolume);
			simulation.heatedCoolantTank = this.getHeatedCoolantTank(waterVolume);
			simulation.waterTank = this.getWaterTank(waterVolume);
			simulation.cooledCoolantTank = this.getCooledCoolantTank(steamVolume);
			simulation.superHeatingElements = heatingElementCount;

			simulation.coolantCoolingEfficiency = coolantCoolingEfficiency;
			simulation.coolantThermalEnthalpy = coolantThermalEnthalpy;

			return simulation;
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
			return this.valvesWidget.getSlider().getValue();
		}

		public void setValveCount(int valveCount)
		{
			this.valvesWidget.getSlider().setValue(valveCount);
		}

		public boolean isUseStruturalGlass()
		{
			return this.useStructuralGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useStructuralGlass)
		{
			this.useStructuralGlassCheckBox.setSelected(useStructuralGlass);
		}

		public boolean isForSodiumCooling()
		{
			return this.forSodiumCoolingCheckBox.isSelected();
		}

		public void setForSodiumCooling(boolean forSodiumCooling)
		{
			this.forSodiumCoolingCheckBox.setSelected(forSodiumCooling);
		}

		public int getSteamHeight()
		{
			return this.steamHeightWidget.getSlider().getValue();
		}

		public void setSteamHeight(int height)
		{
			this.steamHeightWidget.getSlider().setValue(height);
		}

		public int getHeatingElementCount()
		{
			return this.heatingElementsWidget.getSlider().getValue();
		}

		public void SetHeatingElementCount(int heatingElementCount)
		{
			this.heatingElementsWidget.getSlider().setValue(heatingElementCount);
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

	public static class BoilerCoolingSimulation
	{
		public double heatCapacity;
		public long heatedCoolantTank;
		public long cooledCoolantTank;
		public long waterTank;
		public long steamTank;
		public double coolantCoolingEfficiency;
		public double coolantThermalEnthalpy;
		public int superHeatingElements;

		public double heat;
		public double thermalHeat;
		public double boilHeat;
		public double heatOnTemp;
		public boolean needMoreSteamVolume;
		public boolean needMoreSuperHeatingElements;
		public long maxBoil;

		public BoilerCoolingSimulation()
		{

		}

		public void reset()
		{
			this.heat = HeatAPI.AMBIENT_TEMP * this.heatCapacity;
		}

		public void cycleForStableBoil()
		{
			this.reset();

			long prevBoil = -1L;

			while (true)
			{
				this.tick();

				if (prevBoil > -1L && prevBoil >= this.maxBoil)
				{
					break;
				}

				prevBoil = this.maxBoil;
			}

		}

		public void cycleForStableTemp()
		{
			this.reset();

			double prevHeat = -1.0D;

			while (true)
			{
				this.tick();

				if (prevHeat > -1.0D && prevHeat >= this.heat)
				{
					break;
				}

				prevHeat = this.heat;
			}

		}

		public void tick()
		{
			double temp = this.heat / this.heatCapacity;

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

			this.heatOnTemp = (temp - HeatUtils.BASE_BOIL_TEMP) * (this.heatCapacity * MekanismConfig.general.boilerWaterConductivity.get());
			double boilingHeat = this.heatOnTemp;
			double heatingCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * this.superHeatingElements;

			if (this.heatOnTemp > heatingCapacity)
			{
				boilingHeat = heatingCapacity;
				this.needMoreSuperHeatingElements = true;
			}
			else
			{
				this.needMoreSuperHeatingElements = false;
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
