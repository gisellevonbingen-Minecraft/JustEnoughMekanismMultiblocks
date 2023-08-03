package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

public class MatrixCategory extends MultiblockCategory<MatrixCategory.MatrixWidget>
{
	public MatrixCategory(IGuiHelper helper)
	{
		super(helper, Mekanism.rl("matrix"), MekanismLang.MATRIX.translate(), MekanismBlocks.INDUCTION_PORT.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.INDUCTION_CASING.getItemStack());
		consumer.accept(MekanismBlocks.INDUCTION_PORT.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());

		consumer.accept(MekanismBlocks.BASIC_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ADVANCED_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ELITE_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ULTIMATE_INDUCTION_CELL.getItemStack());

		consumer.accept(MekanismBlocks.BASIC_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ELITE_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.getItemStack());
	}

	@Override
	public void setIngredients(MatrixWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends MatrixWidget> getRecipeClass()
	{
		return MatrixWidget.class;
	}

	public static class MatrixWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected IntSliderWithButtons portsWidget;

		public MatrixWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useStructuralGlassCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);

			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 2, 2, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);

			this.updatePortsSliderLimit();
		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();

			int ports = this.getPortCount();
			sides -= ports;

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
				structuralGlasses = 0;
			}

			consumer.accept(new ItemStack(MekanismBlocks.INDUCTION_CASING, casing));
			consumer.accept(new ItemStack(MekanismBlocks.INDUCTION_PORT, ports));
			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));
		}

		@Override
		protected void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int innerVolume = this.getDimensionInnerVolume();
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.inner_volume"), new TranslationTextComponent("text.jei_mekanism_multiblocks.result.blocks", TextUtils.format(innerVolume))));
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsSliderLimit();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdate();
		}

		public void updatePortsSliderLimit()
		{
			IntSliderWidget portsSlider = this.portsWidget.getSlider();
			int ports = portsSlider.getValue();
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setValue(ports);
		}

		protected void onPortsChanged(int ports)
		{
			this.markNeedUpdate();
		}

		public boolean isUseStruturalGlass()
		{
			return this.useStructuralGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useStructuralGlass)
		{
			this.useStructuralGlassCheckBox.setSelected(useStructuralGlass);
		}

		public int getPortCount()
		{
			return this.portsWidget.getSlider().getValue();
		}

		public void setPortCount(int portCount)
		{
			this.portsWidget.getSlider().setValue(portCount);
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
