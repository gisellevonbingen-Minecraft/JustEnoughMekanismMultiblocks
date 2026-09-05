package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.ButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.EnergyEnterScreen;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.DurationTextHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class LasersCategory extends MultiblockCategory<LasersCategory.LaserWidget>
{
	public static final RecipeType<LasersCategory.LaserWidget> RECIPE_TYPE = createRecipeType(Mekanism.rl("lasers"), LaserWidget.class);

	public LasersCategory(IGuiHelper helper)
	{
		super(helper, RECIPE_TYPE, new TranslatableComponent("text.jei_mekanism_multiblocks.building.lasers"), MekanismBlocks.LASER.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(MekanismBlocks.LASER_AMPLIFIER.getItemStack());
		consumer.accept(MekanismBlocks.LASER.getItemStack());
	}

	public static class LaserWidget extends MultiblockWidget
	{
		protected ButtonWidget enterButton;
		protected Map<Block, IntSliderWithButtons> laserWidgets;
		protected Map<Block, FloatingLong> laserUsages;
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

			this.enterButton = new ButtonWidget(0, 0, 0, 0, new TranslatableComponent("text.jei_mekanism_multiblocks.specs.target_energy"));
			this.enterButton.addPressHandler(this::onEnterButtonPress);
			consumer.accept(this.enterButton);

			this.laserWidgets = new HashMap<>();
			this.laserUsages = new HashMap<>();

			IntSliderWithButtons laserWidget = this.createLaserConfig(consumer, MekanismBlocks.LASER.getBlock(), MekanismConfig.usage.laser.get());
			laserWidget.getSlider().setMinValue(1);
			laserWidget.getSlider().setValue(1);
		}

		protected IntSliderWithButtons createLaserConfig(Consumer<AbstractWidget> consumer, Block block, FloatingLong usage)
		{
			IntSliderWithButtons widget = new IntSliderWithButtons(0, 0, 0, 0, "", 1, 0, 100)
			{
				@Override
				protected void updateMessage()
				{
					this.getSlider().setMessage(new TranslatableComponent("%s: %s", new ItemStack(block).getHoverName(), this.getDisplayValue()));
				}
			};
			consumer.accept(widget);
			widget.getSlider().addValueChangeHanlder(this::onLasersChanged);
			this.laserWidgets.put(block, widget);
			this.laserUsages.put(block, usage);
			return widget;
		}

		private void onEnterButtonPress(AbstractButton button)
		{
			EnergyEnterScreen screen = new EnergyEnterScreen(this.enterButton.getMessage(), jules ->
			{
				this.setTargetEnergy(jules.doubleValue());
			});
			screen.setJules(FloatingLong.create(this.targetEnergy));
			Minecraft.getInstance().pushGuiLayer(screen);
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			CompoundTag laserCounts = tag.getCompound("LaserCounts");

			for (Block block : this.laserWidgets.keySet())
			{
				this.setLaserCount(block, laserCounts.getInt(String.valueOf(ForgeRegistries.BLOCKS.getKey(block))));
			}

			this.setTargetEnergy(tag.getDouble("TargetEnergy"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			super.save(tag);

			CompoundTag laserCounts = new CompoundTag();

			for (Block block : this.laserWidgets.keySet())
			{
				laserCounts.putInt(String.valueOf(ForgeRegistries.BLOCKS.getKey(block)), this.getLaserCount(block));
			}

			tag.put("LaserCounts", laserCounts);
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

			int lasers = 0;

			for (Block block : this.laserWidgets.keySet())
			{
				lasers += this.getLaserCount(block);
			}

			int amplifiers = (lasers + 2) / 4;
			consumer.accept(new ItemStack(MekanismBlocks.LASER_AMPLIFIER, amplifiers));

			for (Block block : this.laserWidgets.keySet())
			{
				consumer.accept(new ItemStack(block, this.getLaserCount(block)));
			}

		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			FloatingLong ept = FloatingLong.create(0.0D);

			for (Block block : this.laserWidgets.keySet())
			{
				int laserCount = this.getLaserCount(block);
				ept = ept.plusEqual(this.laserUsages.get(block).multiply(laserCount));
			}

			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.specs.target_energy"), EnergyDisplay.of(FloatingLong.create(this.targetEnergy)).getTextComponent()));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.energy_rate"), new TranslatableComponent("%s/t", EnergyDisplay.of(ept).getTextComponent())));

			long ticks = ept.isZero() ? 0L : FloatingLong.create(this.targetEnergy).divide(ept).ceil().longValue();
			ResultWidget mergeTimeWidget = new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.merge_time"), DurationTextHelper.duration(ticks));
			mergeTimeWidget.setTooltip(DurationTextHelper.ticks(ticks));
			consumer.accept(mergeTimeWidget);
		}

		public int getLaserCount(Block block)
		{
			return this.laserWidgets.get(block).getSlider().getValue();
		}

		public void setLaserCount(Block block, int laserCount)
		{
			this.laserWidgets.get(block).getSlider().setValue(laserCount);
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
