package giselle.jei_mekanism_multiblocks.client.jei.category.evolved;

import java.util.function.Consumer;

import fr.iglee42.evolvedmekanism.EvolvedMekanism;
import fr.iglee42.evolvedmekanism.EvolvedMekanismLang;
import fr.iglee42.evolvedmekanism.config.EMConfig;
import fr.iglee42.evolvedmekanism.registries.EMBlocks;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import giselle.jei_mekanism_multiblocks.client.preview.IPreviewBuilder;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewSelector;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewSelectors;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class APTCategory extends MultiblockCategory<APTCategory.APTWidget>
{
	public static final RecipeType<APTCategory.APTWidget> RECIPE_TYPE = createRecipeType(EvolvedMekanism.rl("apt"), APTWidget.class);

	public APTCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, EvolvedMekanismLang.APT.translate(), new ItemStack(EMBlocks.APT_PORT));
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(new ItemStack(EMBlocks.APT_CASING));
		consumer.accept(new ItemStack(EMBlocks.APT_PORT));
		consumer.accept(new ItemStack(EMBlocks.SUPERCHARGING_ELEMENT));
		consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS));
	}

	public static class APTWidget extends MultiblockWidget
	{
		private static final byte[][] PREVIEW_HORIZONTAL_LAYER = {//
				{0, 0, 1, 1, 1, 0, 0}, //
				{0, 1, 2, 2, 2, 1, 0}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{0, 1, 2, 2, 2, 1, 0}, //
				{0, 0, 1, 1, 1, 0, 0}//
		};
		private static final byte[][] PREVIEW_NARROW_SIDE_LAYER = {//
				{0, 1, 2, 2, 2, 1, 0}, //
				{1, 0, 0, 0, 0, 0, 1}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{1, 0, 0, 0, 0, 0, 1}, //
				{0, 1, 2, 2, 2, 1, 0}//
		};
		private static final byte[][] PREVIEW_WIDE_SIDE_LAYER = {//
				{1, 2, 2, 2, 2, 2, 1}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{2, 0, 0, 0, 0, 0, 2}, //
				{1, 2, 2, 2, 2, 2, 1}//
		};
		private static final byte[][][] PREVIEW_PATTERN = {//
				PREVIEW_HORIZONTAL_LAYER, //
				PREVIEW_NARROW_SIDE_LAYER, //
				PREVIEW_WIDE_SIDE_LAYER, //
				PREVIEW_NARROW_SIDE_LAYER, //
				PREVIEW_HORIZONTAL_LAYER//
		};

		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons superchargingElementsWidget;

		public APTWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 2, this.getSideBlocks()));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.superchargingElementsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.result.supercharging_elements", 0, 0, 25));
			this.superchargingElementsWidget.getSlider().addValueChangeHanlder(this::onSuperchargingElementsChanged);
		}

		@Override
		public boolean canCreatePreview()
		{
			return true;
		}

		@Override
		protected void fillPreview(IPreviewBuilder builder)
		{
			super.fillPreview(builder);

			BlockState casingState = EMBlocks.APT_CASING.defaultState();
			BlockState sideState = this.isUseGlass() ? this.getGlassBlock().defaultBlockState() : casingState;
			BlockState portState = EMBlocks.APT_PORT.defaultState();
			PreviewSelector casingPositions = PreviewSelectors.volumePattern(PREVIEW_PATTERN, value -> value == 1);
			PreviewSelector sidePositions = PreviewSelectors.volumePattern(PREVIEW_PATTERN, value -> value == 2);
			PreviewSelector portPositions = PreviewSelectors.shellSidesCCW().and(sidePositions);

			builder.setBlock(casingPositions, casingState);
			builder.setBlock(sidePositions, sideState);
			builder.replaceBlock(portPositions, sideState, portState, this.getPortCount());
			builder.setBlock(PreviewSelectors.planeSpiralCCW(context -> new BlockPos(3, 1, 3), context -> this.getSuperchargingElementsCount()), EMBlocks.SUPERCHARGING_ELEMENT.defaultState());
		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);
			int edges = this.getEdgeBlocks();
			int ports = this.getPortCount();
			int sides = this.getSideBlocks() - ports;
			int superchargingElements = this.getSuperchargingElementsCount();
			int casings = 0;
			int glasses = 0;

			if (this.isUseGlass())
			{
				casings = edges;
				glasses = sides;
			}
			else
			{
				casings = edges + sides;
				glasses = 0;
			}
			consumer.accept(new ItemStack(EMBlocks.APT_CASING, casings));
			consumer.accept(new ItemStack(EMBlocks.APT_PORT, ports));
			if (superchargingElements > 0)
			{
				consumer.accept(new ItemStack(EMBlocks.SUPERCHARGING_ELEMENT, superchargingElements));
			}
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.processing_speed"), Component.literal("x" + (this.getSuperchargingElementsCount() + 1))));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_capacity"), EnergyDisplay.of(EMConfig.general.aptEnergyStorage.get()).getTextComponent()));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMB(EMConfig.general.aptInputStorage.get())));
		}

		protected void onPortsChanged(int ports)
		{
			this.markNeedUpdate();
		}

		protected void onSuperchargingElementsChanged(int superchargingElements)
		{
			this.markNeedUpdate();
		}

		@Override
		public int getEdgeBlocks()
		{
			return 52;
		}

		@Override
		public int getSideBlocks()
		{
			return 86;
		}

		public int getPortCount()
		{
			return this.portsWidget.getSlider().getValue();
		}

		public void setPortCount(int portCount)
		{
			this.portsWidget.getSlider().setValue(portCount);
		}

		public int getSuperchargingElementsCount()
		{
			return this.superchargingElementsWidget.getSlider().getValue();
		}

		public void setSuperchargingElementsCount(int superchargingElements)
		{
			this.superchargingElementsWidget.getSlider().setValue(superchargingElements);
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 7;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 7;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 7;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 7;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 5;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 5;
		}

		@Override
		public Block getGlassBlock()
		{
			return MekanismBlocks.STRUCTURAL_GLASS.get();
		}

	}

}
