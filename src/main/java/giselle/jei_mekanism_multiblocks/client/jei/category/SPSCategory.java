package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.preview.IPreviewBuilder;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewSelector;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewSelectors;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SPSCategory extends MultiblockCategory<SPSCategory.SPSWidget>
{
	public static final RecipeType<SPSCategory.SPSWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("sps"), SPSWidget.class);

	public SPSCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, MekanismLang.SPS.translate(), MekanismBlocks.SPS_PORT.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.SPS_CASING.getItemStack());
		consumer.accept(MekanismBlocks.SPS_PORT.getItemStack());
		consumer.accept(MekanismBlocks.SUPERCHARGED_COIL.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());
	}

	public static class SPSWidget extends MultiblockWidget
	{
		private static final byte[][] PREVIEW_SHELL_PATTERN = {//
				{0, 0, 1, 1, 1, 0, 0}, //
				{0, 1, 2, 2, 2, 1, 0}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{1, 2, 2, 2, 2, 2, 1}, //
				{0, 1, 2, 2, 2, 1, 0}, //
				{0, 0, 1, 1, 1, 0, 0}//
		};

		protected IntSliderWithButtons portsWidget;

		public SPSWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 3, this.getSideBlocks()));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
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
		public boolean canCreatePreview()
		{
			return true;
		}

		@Override
		protected void fillPreview(IPreviewBuilder builder)
		{
			super.fillPreview(builder);

			BlockPos superchargedPortPos = new BlockPos(0, 3, 3);
			BlockPos superchangedCoilPos = new BlockPos(1, 3, 3);

			BlockState casingState = MekanismBlocks.SPS_CASING.getBlock().defaultBlockState();
			BlockState sideState = this.isUseGlass() ? this.getGlassBlock().defaultBlockState() : casingState;
			BlockState portState = MekanismBlocks.SPS_PORT.getBlock().defaultBlockState();
			PreviewSelector casingPositions = PreviewSelectors.shellPattern(PREVIEW_SHELL_PATTERN, value -> value == 1);
			PreviewSelector sidePositions = PreviewSelectors.shellPattern(PREVIEW_SHELL_PATTERN, value -> value == 2);
			PreviewSelector portPositions = PreviewSelectors.shellSidesCCW().and(sidePositions);

			builder.setBlock(casingPositions, casingState);
			builder.setBlock(sidePositions, sideState);
			builder.setBlock(superchargedPortPos, portState);
			builder.setBlock(superchangedCoilPos, Attribute.setFacing(MekanismBlocks.SUPERCHARGED_COIL.getBlock().defaultBlockState(), Direction.EAST));
			builder.replaceBlock(portPositions, sideState, portState, Math.max(0, this.getPortCount() - 1));
		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int edges = this.getEdgeBlocks();
			int sides = this.getSideBlocks();
			int ports = this.getPortCount();
			sides -= ports;

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

			consumer.accept(new ItemStack(MekanismBlocks.SPS_CASING, casings));
			consumer.accept(new ItemStack(MekanismBlocks.SPS_PORT, ports));
			consumer.accept(new ItemStack(MekanismBlocks.SUPERCHARGED_COIL));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int inputPerAnimatter = MekanismConfig.general.spsInputPerAntimatter.get();
			FloatingLong energyPerPolonium = MekanismConfig.general.spsEnergyPerInput.get();
			FloatingLong energyPerAntimatter = energyPerPolonium.multiply(inputPerAnimatter);
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.energy_per_antimatter"), new TranslatableComponent("%s/%s", EnergyDisplay.of(energyPerAntimatter).getTextComponent(), "mB")));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.energy_per_polonium"), new TranslatableComponent("%s/%s", EnergyDisplay.of(energyPerPolonium).getTextComponent(), "mB")));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.antimatter_per_polonium"), VolumeTextHelper.formatMB(inputPerAnimatter)));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMB(inputPerAnimatter * 2L)));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.output_tank"), VolumeTextHelper.formatMB(1_000)));
		}

		protected void onPortsChanged(int ports)
		{
			this.markNeedUpdate();
		}

		@Override
		public int getEdgeBlocks()
		{
			return 60;
		}

		@Override
		public int getSideBlocks()
		{
			return 126;
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
			return 7;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 7;
		}

		@Override
		public Block getGlassBlock()
		{
			return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
		}

	}

}
