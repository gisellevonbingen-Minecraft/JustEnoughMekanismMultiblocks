package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

public class DynamicTankCategory extends MultiblockCategory<DynamicTankCategory.DynamicTankWidget>
{
	public DynamicTankCategory(IGuiHelper helper)
	{
		super(helper, Mekanism.rl("dynamic_tank"), MekanismLang.DYNAMIC_TANK.translate(), MekanismBlocks.DYNAMIC_VALVE.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.DYNAMIC_TANK.getItemStack());
		consumer.accept(MekanismBlocks.DYNAMIC_VALVE.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());
	}

	@Override
	public void setIngredients(DynamicTankWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends DynamicTankWidget> getRecipeClass()
	{
		return DynamicTankWidget.class;
	}

	public static class DynamicTankWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected IntSliderWithButtons valvesWidget;

		public DynamicTankWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useStructuralGlassCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 0, 0));
			this.valvesWidget.getSlider().addValueChangeHanlder(this::onValvesChanged);

			this.updateValvesSliderLimit();
			this.setValveCount(2);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateValvesSliderLimit();
		}

		public void updateValvesSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getValue();
			valvesSlider.setMaxValue(this.getSideBlocks());
			valvesSlider.setValue(valves);
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdate();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
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

			int tanks = 0;
			int structuralGlasses = 0;

			if (this.isUseStruturalGlass())
			{
				tanks = corners;
				structuralGlasses = sides;
			}
			else
			{
				tanks = corners + sides;
			}

			consumer.accept(new ItemStack(MekanismBlocks.DYNAMIC_TANK, tanks));
			consumer.accept(new ItemStack(MekanismBlocks.DYNAMIC_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));
		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int volume = this.getDimensionVolume();
			long fluidCapacity = volume * MekanismConfig.general.dynamicTankFluidPerTank.get();
			long chemicalCapacity = volume * MekanismConfig.general.dynamicTankChemicalPerTank.get();
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.fluid_tank"), VolumeTextHelper.formatMB(fluidCapacity)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.chemical_tank"), VolumeTextHelper.formatMB(chemicalCapacity)));
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
			return 3;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

	}

}
