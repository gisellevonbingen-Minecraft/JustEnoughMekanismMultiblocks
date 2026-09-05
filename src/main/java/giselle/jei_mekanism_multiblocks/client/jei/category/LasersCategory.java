package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.folumo.mekanism_lasers.common.registry.BlockRegistry;
import com.folumo.mekanism_lasers.common.tier.LaserTier;

import giselle.jei_mekanism_multiblocks.client.TooltipHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.EnergyEnterScreen;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
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
import net.minecraft.core.registries.BuiltInRegistries;
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
		consumer.accept(new ItemStack(MekanismBlocks.LASER_AMPLIFIER));
		consumer.accept(new ItemStack(MekanismBlocks.LASER));

		if (JEI_MekanismMultiblocks.MekanismLasersLoaded)
		{
			consumer.accept(new ItemStack(BlockRegistry.BASIC_LASER));
			consumer.accept(new ItemStack(BlockRegistry.BASIC_TOGGLEABLE_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ADVANCED_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ADVANCED_TOGGLEABLE_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ELITE_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ELITE_TOGGLEABLE_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ULTIMATE_LASER));
			consumer.accept(new ItemStack(BlockRegistry.ULTIMATE_TOGGLEABLE_LASER));
		}

	}

	public static class LaserWidget extends MultiblockWidget
	{
		protected ButtonWidget enterButton;
		protected Map<Block, IntSliderWithButtons> laserWidgets;
		protected Map<Block, Long> laserUsages;
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

			this.laserWidgets = new HashMap<>();
			this.laserUsages = new HashMap<>();

			IntSliderWithButtons laserWidget = this.createLaserConfig(consumer, MekanismBlocks.LASER.get(), MekanismConfig.usage.laser.get());

			if (JEI_MekanismMultiblocks.MekanismLasersLoaded)
			{
				this.createLaserConfig(consumer, BlockRegistry.BASIC_LASER.get(), LaserTier.BASIC.getEnergyUsage().getAsLong());
				this.createLaserConfig(consumer, BlockRegistry.ADVANCED_LASER.get(), LaserTier.ADVANCED.getEnergyUsage().getAsLong());
				this.createLaserConfig(consumer, BlockRegistry.ELITE_LASER.get(), LaserTier.ELITE.getEnergyUsage().getAsLong());
				this.createLaserConfig(consumer, BlockRegistry.ULTIMATE_LASER.get(), LaserTier.ULTIMATE.getEnergyUsage().getAsLong());
			}
			else
			{
				laserWidget.getSlider().setMinValue(1);
			}

			laserWidget.getSlider().setValue(1);
		}

		protected IntSliderWithButtons createLaserConfig(Consumer<AbstractWidget> consumer, Block block, long usage)
		{
			IntSliderWithButtons widget = new IntSliderWithButtons(0, 0, 0, 0, "", 1, 0, 100)
			{
				@Override
				protected void updateMessage()
				{
					this.getSlider().setMessage(Component.translatable("%s: %s", new ItemStack(block).getHoverName(), this.getDisplayValue()));
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
				this.setTargetEnergy(jules);
			});
			screen.setJules(this.targetEnergy);
			Minecraft.getInstance().pushGuiLayer(screen);
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			CompoundTag laserCounts = tag.getCompound("LaserCounts");

			for (Block block : this.laserWidgets.keySet())
			{
				this.setLaserCount(block, laserCounts.getInt(String.valueOf(BuiltInRegistries.BLOCK.getKey(block))));
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
				laserCounts.putInt(String.valueOf(BuiltInRegistries.BLOCK.getKey(block)), this.getLaserCount(block));
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

			long ept = 0L;

			for (Block block : this.laserWidgets.keySet())
			{
				int laserCount = this.getLaserCount(block);
				ept += this.laserUsages.get(block) * laserCount;
			}

			EnergyUnit displayUnit = EnergyUnit.getConfigured();
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.specs.target_energy"), UnitDisplayUtils.getDisplayShort(this.targetEnergy / displayUnit.getConversion(), displayUnit)));
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.energy_rate"), Component.translatable("%s/t", EnergyDisplay.of(ept).getTextComponent())));

			long ticks = ept == 0L ? 0L : (long) Math.ceil(this.targetEnergy / (double) ept);
			ResultWidget mergeTimeWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.merge_time"), DurationTextHelper.duration(ticks));
			mergeTimeWidget.setTooltip(TooltipHelper.createMessageOnly(DurationTextHelper.ticks(ticks)));
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
