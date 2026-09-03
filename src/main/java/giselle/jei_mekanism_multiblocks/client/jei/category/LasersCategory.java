package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.TooltipHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.EnergyEnterScreen;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.DurationTextHelper;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LasersCategory extends MultiblockCategory<LasersCategory.LaserWidget>
{
	public static final RecipeType<LasersCategory.LaserWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("lasers"), LaserWidget.class);

	public LasersCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, Component.translatable("text.jei_mekanism_multiblocks.building.lasers"), new ItemStack(MekanismBlocks.LASER));
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(new ItemStack(MekanismBlocks.LASER));
		consumer.accept(new ItemStack(MekanismBlocks.LASER_AMPLIFIER));
	}

	public static class LaserWidget extends MultiblockWidget
	{
		protected ButtonWidget enterButton;
		protected IntSliderWithButtons laserWidget;
		private double targetEnergy;

		@Override
		protected boolean isUseDimensionWidget(IntSliderWithButtons widget)
		{
			return false;
		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			this.enterButton = new ButtonWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.target_energy"));
			this.enterButton.addPressHandler(this::onEnterButtonPress);
			consumer.accept(this.enterButton);

			consumer.accept(this.laserWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.lasers", 1, 1, 100));
			this.laserWidget.getSlider().addValueChangeHanlder(this::onLasersChanged);

		}

		private void onEnterButtonPress(AbstractButton button)
		{
			EnergyEnterScreen screen = new EnergyEnterScreen(this.enterButton.getMessage(), this::setTargetEnergy);
			screen.setJules(this.targetEnergy);
			Minecraft.getInstance().pushGuiLayer(screen);
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			this.setLaserCount(tag.getInt("LaserCount"));
			this.setTargetEnergy(tag.getDouble("TargetEnergy"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			super.save(tag);

			tag.putInt("LaserCount", this.getLaserCount());
			tag.putDouble("TargetEnergy", this.getTargetEnergy());
		}

		protected void onLasersChanged(int lasers)
		{
			this.markNeedUpdate();
		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int lasers = this.getLaserCount();
			int amplifiers = (lasers + 2) / 4;

			consumer.accept(new ItemStack(MekanismBlocks.LASER, lasers));
			consumer.accept(new ItemStack(MekanismBlocks.LASER_AMPLIFIER, amplifiers));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int lasers = this.getLaserCount();
			long ept = MekanismConfig.usage.laser.get() * lasers;
			long ticks = (long) Math.ceil(this.targetEnergy / (double) ept);
			
			EnergyUnit displayUnit = EnergyUnit.getConfigured();
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.specs.target_energy"), UnitDisplayUtils.getDisplayShort(this.targetEnergy / displayUnit.getConversion(), displayUnit)));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_rate"), Component.translatable("%s/t", EnergyDisplay.of(ept).getTextComponent())));

			ResultWidget mergeTimeWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.merge_time"), DurationTextHelper.duration(ticks));
			mergeTimeWidget.setTooltip(TooltipHelper.createMessageOnly(DurationTextHelper.ticks(ticks)));
			consumer.accept(mergeTimeWidget);
		}

		public int getLaserCount()
		{
			return this.laserWidget.getSlider().getValue();
		}

		public void setLaserCount(int laserCount)
		{
			this.laserWidget.getSlider().setValue(laserCount);
		}

		public double getTargetEnergy()
		{
			return this.targetEnergy;
		}

		public void setTargetEnergy(double targetEnergy)
		{
			this.targetEnergy = Math.max(0.0D, targetEnergy);
			this.markNeedUpdate();
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 1;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 1;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 1;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 1;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 1;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 1;
		}

		@Override
		public Block getGlassBlock()
		{
			return null;
		}

	}

}
