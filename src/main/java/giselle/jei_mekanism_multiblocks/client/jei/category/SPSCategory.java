package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class SPSCategory extends MultiblockCategory<SPSCategory.SPSWidget>
{
	public static final RecipeType<SPSCategory.SPSWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("sps"), SPSWidget.class);

	public SPSCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, MekanismLang.SPS.translate(), new ItemStack(MekanismBlocks.SPS_PORT));
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(new ItemStack(MekanismBlocks.SPS_CASING));
		consumer.accept(new ItemStack(MekanismBlocks.SPS_PORT));
		consumer.accept(new ItemStack(MekanismBlocks.SUPERCHARGED_COIL));
		consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS));
	}

	public static class SPSWidget extends MultiblockWidget
	{
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

			consumer.accept(new ItemStack(MekanismBlocks.SPS_CASING, casing));
			consumer.accept(new ItemStack(MekanismBlocks.SPS_PORT, ports));
			consumer.accept(new ItemStack(MekanismBlocks.SUPERCHARGED_COIL));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int inputPerAnimatter = MekanismConfig.general.spsInputPerAntimatter.get();
			long energyPerPolonium = MekanismConfig.general.spsEnergyPerInput.get();
			long energyPerAntimatter = MathUtils.multiplyClamped(energyPerPolonium, inputPerAnimatter);
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_per_antimatter"), Component.translatable("%s/%s", EnergyDisplay.of(energyPerAntimatter).getTextComponent(), "mB")));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_per_polonium"), Component.translatable("%s/%s", EnergyDisplay.of(energyPerPolonium).getTextComponent(), "mB")));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.antimatter_per_polonium"), VolumeTextHelper.formatMB(inputPerAnimatter)));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMB(inputPerAnimatter * 2L)));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.output_tank"), VolumeTextHelper.formatMB(MekanismConfig.general.spsOutputTankCapacity.get())));
		}

		protected void onPortsChanged(int ports)
		{
			this.markNeedUpdate();
		}

		@Override
		public int getCornerBlocks()
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
			return MekanismBlocks.STRUCTURAL_GLASS.get();
		}

	}

}
