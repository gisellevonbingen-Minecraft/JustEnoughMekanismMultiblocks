package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import fr.iglee42.evolvedmekanism.registries.EMBlocks;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockInductionProvider;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class MatrixCategory extends MultiblockCategory<MatrixCategory.MatrixWidget>
{
	public static final RecipeType<MatrixCategory.MatrixWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("matrix"), MatrixWidget.class);

	public MatrixCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, MekanismLang.MATRIX.translate(), MekanismBlocks.INDUCTION_PORT.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.INDUCTION_CASING.getItemStack());
		consumer.accept(MekanismBlocks.INDUCTION_PORT.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());

		consumer.accept(MekanismBlocks.BASIC_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.BASIC_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ADVANCED_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ELITE_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ELITE_INDUCTION_PROVIDER.getItemStack());
		consumer.accept(MekanismBlocks.ULTIMATE_INDUCTION_CELL.getItemStack());
		consumer.accept(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.getItemStack());

		if (JEI_MekanismMultiblocks.EvoledMekanismLoaded && JEI_MekanismMultiblocks_Config.CLIENT.evolvedMatrixVisible.get())
		{
			for (IBlockProvider block : EMBlocks.BLOCKS.getAllBlocks())
			{
				Item item = block.asItem();

				if (item instanceof ItemBlockInductionCell || item instanceof ItemBlockInductionProvider)
				{
					consumer.accept(block.getItemStack());
				}

			}

		}

	}

	public static class MatrixWidget extends MultiblockWidget
	{
		protected IntSliderWithButtons portsWidget;

		public MatrixWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 2, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);

			this.updatePortsSliderLimit();
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			this.setPortCount(tag.getInt("PortCount"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			super.save(tag);

			tag.putInt("PortCount", this.getPortCount());
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
			int glasses = 0;

			if (this.isUseGlass())
			{
				casing = corners;
				glasses = sides;
			}
			else
			{
				casing = corners + sides;
				glasses = 0;
			}

			consumer.accept(new ItemStack(MekanismBlocks.INDUCTION_CASING, casing));
			consumer.accept(new ItemStack(MekanismBlocks.INDUCTION_PORT, ports));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int innerVolume = this.getDimensionInnerVolume();
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.inner_volume"), Component.translatable("text.jei_mekanism_multiblocks.result.blocks", TextUtils.format(innerVolume))));
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsSliderLimit();
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

		@Override
		public Block getGlassBlock()
		{
			return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
		}

	}

}
