package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class EvaporationPlantCategory extends MultiblockCategory<EvaporationPlantCategory.EvaporationPlantWidget>
{
	public static final RecipeType<EvaporationPlantCategory.EvaporationPlantWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("evaporation_plant"), EvaporationPlantWidget.class);

	public EvaporationPlantCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, MekanismLang.EVAPORATION_PLANT.translate(), MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack());
		consumer.accept(MekanismBlocks.THERMAL_EVAPORATION_VALVE.getItemStack());
		consumer.accept(MekanismBlocks.THERMAL_EVAPORATION_BLOCK.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());

		if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
		{
			consumer.accept(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack());
		}

	}

	public static class EvaporationPlantWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useAdvancedSolarGeneratorCheckBox;
		protected CheckBoxWidget useFuelwoodHeaterCheckBox;
		protected IntSliderWithButtons valvesWidget;

		private boolean needHeatSource;
		private int fuelwoodHeaters;

		public EvaporationPlantWidget()
		{

		}

		@Override
		public int getSideBlocks()
		{
			// 1 Controller
			// 4 Empty top inner
			return super.getSideBlocks() - 5;
		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack().getHoverName()), true));
				this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseAdvancedSolarGeneratorChanged);
			}
			else
			{
				this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.empty(), false);
				this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseAdvancedSolarGeneratorChanged);
			}

			consumer.accept(this.useFuelwoodHeaterCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", new ItemStack(MekanismBlocks.FUELWOOD_HEATER).getHoverName()), true));
			this.useFuelwoodHeaterCheckBox.addSelectedChangedHandler(this::onUseFuelwoodHeaterChanged);

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
			this.valvesWidget.getSlider().addValueChangeHanlder(this::onValvesChanged);

			this.onThermalModelChanged();
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			this.setUseAdvancedSolarGenerator(tag.getBoolean("UseAdvancedSolarGenerator"));
			this.setUseFuelwoodHeater(tag.getBoolean("UseFuelwoodHeater"));
			this.setValveCount(tag.getInt("ValveCount"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			super.save(tag);

			tag.putBoolean("UseAdvancedSolarGenerator", this.isUseAdvancedSolarGenerator());
			tag.putBoolean("UseFuelwoodHeater", this.isUseFuelwoodHeater());
			tag.putInt("ValveCount", this.getValveCount());
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.onThermalModelChanged();
		}

		public void updateValveSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int minValves = valvesSlider.getMinValue();
			int valves = valvesSlider.getValue();
			valvesSlider.setMinValue(2 + (this.isNeedHeatSource() ? (this.isUseFuelwoodHeater() ? this.getFuelwoodHeaters() : 1) : 0));
			valvesSlider.setMaxValue(this.getSideBlocks());
			valvesSlider.setValue(valves + (valvesSlider.getMinValue() - minValves));
		}

		protected void onThermalModelChanged()
		{
			double requiredHeat = this.getMaxMultiplierHeat(0.0D);
			this.needHeatSource = requiredHeat > 0.0D;
			this.fuelwoodHeaters = 0;

			if (this.isNeedHeatSource())
			{
				if (this.isUseFuelwoodHeater())
				{
					double heatPerTick = MekanismConfig.general.heatPerFuelTick.get() * MekanismConfig.general.fuelwoodTickMultiplier.get();
					this.fuelwoodHeaters = Mth.ceil(requiredHeat / heatPerTick);
				}

			}

			this.updateValveSliderLimit();
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdate();
		}

		@Override
		protected void onUseGlassChanged(boolean useGlass)
		{
			super.onUseGlassChanged(useGlass);
		}

		protected void onUseAdvancedSolarGeneratorChanged(boolean useAdvancedSolarGenerator)
		{
			this.markNeedUpdate();

			this.onThermalModelChanged();
		}

		protected void onUseFuelwoodHeaterChanged(boolean useFuelwoodHeater)
		{
			this.markNeedUpdate();

			this.onThermalModelChanged();
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
			int glasses = 0;
			int advancedSolarGenerators = 0;

			if (this.isUseGlass())
			{
				casing = corners;
				glasses = sides;

				if (this.isUseAdvancedSolarGenerator())
				{
					// Replace top corner to solar generator
					casing -= 4;
					advancedSolarGenerators += 4;
				}
				else
				{
					// Replace top side to glass
					casing -= 8;
					glasses += 8;
				}

			}
			else
			{
				// Remove top vertices
				casing = corners + sides - 4;

				if (this.isUseAdvancedSolarGenerator())
				{
					advancedSolarGenerators += 4;
				}

			}

			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 1));
			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, casing));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(new ItemStack(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, advancedSolarGenerators));
			}

			if (this.isNeedHeatSource())
			{
				if (this.isUseFuelwoodHeater())
				{
					consumer.accept(new ItemStack(MekanismBlocks.FUELWOOD_HEATER, this.getFuelwoodHeaters()));
				}
				else
				{
					consumer.accept(new ItemStack(MekanismBlocks.RESISTIVE_HEATER));
				}

			}

		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			long dimHeight = this.getDimensionHeight();
			long inputCapacity = dimHeight * 4 * MekanismConfig.general.evaporationFluidPerTank.get();
			long outputCapacity = MekanismConfig.general.evaporationOutputTankCapacity.get();
			double maxTemp = EvaporationMultiblockData.MAX_MULTIPLIER_TEMP;
			double maxSpeed = (maxTemp - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() * ((double) dimHeight / this.getDimensionHeightMax());
			ResultWidget speedWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.max_speed"), Component.literal("x" + TextUtils.format(maxSpeed)));
			speedWidget.setTooltip(Component.translatable("text.jei_mekanism_multiblocks.tooltip.when_temp_ge", MekanismUtils.getTemperatureDisplay(maxTemp, TemperatureUnit.KELVIN, true)));
			consumer.accept(speedWidget);
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMB(inputCapacity)));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.output_tank"), VolumeTextHelper.formatMB(outputCapacity)));

			if (this.isNeedHeatSource() && !this.isUseFuelwoodHeater())
			{
				this.createRequiredHeaterEnergyWidget(consumer);
			}

		}

		private void createRequiredHeaterEnergyWidget(Consumer<AbstractWidget> consumer)
		{
			FloatingLong plainRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.AMBIENT_TEMP);
			FloatingLong coldestRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.getAmbientTemp(Integer.MIN_VALUE));
			FloatingLong hotestRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.getAmbientTemp(Integer.MAX_VALUE));
			ResultWidget requiredEnergyWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.required_heater_usage"), Component.translatable("%s/t", EnergyDisplay.of(plainRequiredEnergy).getTextComponent()));
			Component heaterName = new ItemStack(MekanismBlocks.RESISTIVE_HEATER).getHoverName();
			Component valveName = new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_VALVE).getHoverName();
			requiredEnergyWidget.setTooltip(//
					Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.plain", Component.translatable("%s %s/t", TextUtils.format(plainRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
					Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.coldest", Component.translatable("%s %s/t", TextUtils.format(coldestRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
					Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.hottest", Component.translatable("%s %s/t", TextUtils.format(hotestRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
					Component.translatable("text.jei_mekanism_multiblocks.tooltip.heater_near_and_1_sink_1", heaterName, valveName), //
					Component.translatable("text.jei_mekanism_multiblocks.tooltip.heater_near_and_1_sink_2", heaterName, valveName));
			consumer.accept(requiredEnergyWidget);
		}

		public FloatingLong getRequiredHeaterEnergy(double ambientTemp)
		{
			double heat = this.getMaxMultiplierHeat(ambientTemp);
			return ResistiveHeaterCategory.getHeatTransferableEnergy(ambientTemp, heat, HeatAPI.DEFAULT_INVERSE_CONDUCTION).ceil();
		}

		public double getMaxMultiplierHeat(double ambientTemp)
		{
			int activeSolars = this.isUseAdvancedSolarGenerator() ? 4 : 0;
			double heatCapacity = this.getDimensionHeight() * MekanismConfig.general.evaporationHeatCapacity.get();
			double gain = activeSolars * MekanismConfig.general.evaporationSolarMultiplier.get() * heatCapacity;
			double loss = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(EvaporationMultiblockData.MAX_MULTIPLIER_TEMP - ambientTemp)) * heatCapacity;
			return loss - gain;
		}

		public int getValveCount()
		{
			return this.valvesWidget.getSlider().getValue();
		}

		public void setValveCount(int valveCount)
		{
			this.valvesWidget.getSlider().setValue(valveCount);
		}

		public boolean isUseAdvancedSolarGenerator()
		{
			return JEI_MekanismMultiblocks.MekanismGeneratorsLoaded && this.useAdvancedSolarGeneratorCheckBox.isSelected();
		}

		public void setUseAdvancedSolarGenerator(boolean useAdvancedSolarGenerator)
		{
			this.useAdvancedSolarGeneratorCheckBox.setSelected(useAdvancedSolarGenerator);
		}

		public boolean isUseFuelwoodHeater()
		{
			return this.useFuelwoodHeaterCheckBox.isSelected();
		}

		public void setUseFuelwoodHeater(boolean useFuelwoodHeater)
		{
			this.useFuelwoodHeaterCheckBox.setSelected(useFuelwoodHeater);
		}

		public boolean isNeedHeatSource()
		{
			return this.needHeatSource;
		}

		public int getFuelwoodHeaters()
		{
			return this.fuelwoodHeaters;
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 4;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 4;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 4;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 4;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 3;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

		@Override
		public Block getGlassBlock()
		{
			return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
		}

	}

}
