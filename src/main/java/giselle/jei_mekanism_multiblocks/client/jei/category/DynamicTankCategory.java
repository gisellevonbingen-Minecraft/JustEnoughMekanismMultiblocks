package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
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
		super(helper, "dynamic_tank", null);
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
		private IntSliderWithButtons valvesWidget;
		private CheckBoxWidget useStructuralGlassCheckBox;

		public DynamicTankWidget()
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
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateValveSliderLimit();
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

		@Override
		protected void collectCost(Consumer<ItemStack> consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int inners = this.getInnerBlocks();

			int tanks = 0;
			int structuralGlasses = 0;
			int valves = this.getValveCount();

			if (this.isUseStruturalGlass())
			{
				tanks = corners;
				structuralGlasses = inners - valves;
			}
			else
			{
				tanks = corners + inners - valves;
			}

			consumer.accept(new ItemStack(MekanismBlocks.DYNAMIC_TANK, tanks));
			consumer.accept(new ItemStack(MekanismBlocks.DYNAMIC_VALVE, valves));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));
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
