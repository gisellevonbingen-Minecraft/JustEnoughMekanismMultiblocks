package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mekanism.common.registries.MekanismBlocks;
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
		private IntSliderWithButtons valvesWidget;
		private CheckBoxWidget useStructuralGlassCheckBox;
		private CheckBoxWidget useAdvancedSolarGeneratorCheckBox;

		public ThermalEvaporationPlantWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.valves", 0, 0, 0, this::onValvesChanged));
			this.updateValveSliderLimit();
			this.valvesWidget.getSlider().setIntValue(2);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true, this::onUseStructuralGlassChanged));

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.use_things", GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack().getHoverName()), true, this::onUseStructuralGlassChanged));
			}
			else
			{
				this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, StringTextComponent.EMPTY, false, this::onUseStructuralGlassChanged);
			}

		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateValveSliderLimit();
		}

		@Override
		public int getInnerBlocks()
		{
			// 1 Controller
			// 4 Empty Top
			return super.getInnerBlocks() - 5;
		}

		private void updateValveSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getIntValue();
			valvesSlider.setMaxValue(this.getInnerBlocks());
			valvesSlider.setIntValue(valves);
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdateCost();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdateCost();
		}

		protected void onUseAdvancedSolarGeneratorChanged(boolean useAdvancedSolarGenerator)
		{
			this.markNeedUpdateCost();
		}

		@Override
		protected void collectCost(Consumer<ItemStack> consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int inners = this.getInnerBlocks();

			int valves = this.getValveCount();
			int blocks = 0;
			int structuralGlasses = 0;
			int advancedSolarGenerators = 0;

			if (this.isUseStruturalGlass())
			{
				blocks = corners;
				structuralGlasses = inners - valves;
			}
			else
			{
				blocks = corners + inners - valves;
			}

			if (this.isUseAdvancedSolarGenerator())
			{
				blocks -= 4;
				advancedSolarGenerators += 4;
			}

			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 1));
			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, blocks));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));

			if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded)
			{
				consumer.accept(new ItemStack(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, advancedSolarGenerators));
			}

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
