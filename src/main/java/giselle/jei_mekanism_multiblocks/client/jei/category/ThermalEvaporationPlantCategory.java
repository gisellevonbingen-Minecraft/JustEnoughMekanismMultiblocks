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
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ThermalEvaporationPlantCategory extends MultiblockCategory<ThermalEvaporationPlantCategory.ThermalEvaporationPlantWidget>
{
	public ThermalEvaporationPlantCategory(IGuiHelper helper)
	{
		super(helper, "thermal_evaporation_plant", null);
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

	@Override
	public void setIngredients(ThermalEvaporationPlantWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends ThermalEvaporationPlantWidget> getRecipeClass()
	{
		return ThermalEvaporationPlantWidget.class;
	}

	public static class ThermalEvaporationPlantWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected CheckBoxWidget useAdvancedSolarGeneratorCheckBox;
		protected IntSliderWithButtons valvesWidget;

		public ThermalEvaporationPlantWidget()
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
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack().getHoverName()), true));
				this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			}
			else
			{
				this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, StringTextComponent.EMPTY, false);
				this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			}

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
			this.valvesWidget.getSlider().addIntValueChangeHanlder(this::onValvesChanged);

			this.updateValveSliderLimit();
			this.setValveCount(2);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateValveSliderLimit();
		}

		public void updateValveSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getIntValue();
			valvesSlider.setIntMaxValue(this.getSideBlocks());
			valvesSlider.setIntValue(valves);
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdate();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdate();
		}

		protected void onUseAdvancedSolarGeneratorChanged(boolean useAdvancedSolarGenerator)
		{
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
			int advancedSolarGenerators = 0;

			if (this.isUseStruturalGlass())
			{
				casing = corners;
				structuralGlasses = sides;

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
					structuralGlasses += 8;
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
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(new ItemStack(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, advancedSolarGenerators));
			}

		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			long dimHeight = this.getDimensionHeight();
			long inputCapacity = dimHeight * 4 * EvaporationMultiblockData.FLUID_PER_TANK;
			long outputCapacity = 10_000;
			double outputMultiplier = (EvaporationMultiblockData.MAX_MULTIPLIER_TEMP - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() * ((double) dimHeight / this.getDimensionHeightMax());
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.processing_speed"), new StringTextComponent("x" + TextUtils.format(outputMultiplier))));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMilliBuckets(inputCapacity)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.output_tank"), VolumeTextHelper.formatMilliBuckets(outputCapacity)));
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

		public boolean isUseAdvancedSolarGenerator()
		{
			return JEI_MekanismMultiblocks.MekanismGeneratorsLoaded && this.useAdvancedSolarGeneratorCheckBox.isSelected();
		}

		public void setUseAdvancedSolarGenerator(boolean useAdvancedSolarGenerator)
		{
			this.useStructuralGlassCheckBox.setSelected(useAdvancedSolarGenerator);
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

	}

}
