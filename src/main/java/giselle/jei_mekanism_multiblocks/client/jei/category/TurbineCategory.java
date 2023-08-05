package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.Mod2IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.jei.CostWidget;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class TurbineCategory extends MultiblockCategory<TurbineCategory.TurbineWidget>
{
	public TurbineCategory(IGuiHelper helper)
	{
		super(helper, MekanismGenerators.rl("turbine"), TurbineWidget.class, GeneratorsLang.TURBINE.translate(), GeneratorsBlocks.TURBINE_VALVE.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.TURBINE_CASING.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_VALVE.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_VENT.getItemStack());
		consumer.accept(GeneratorsBlocks.ROTATIONAL_COMPLEX.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_ROTOR.getItemStack());
		consumer.accept(GeneratorsItems.TURBINE_BLADE.getItemStack());
		consumer.accept(MekanismBlocks.PRESSURE_DISPERSER.getItemStack());
		consumer.accept(GeneratorsBlocks.ELECTROMAGNETIC_COIL.getItemStack());
		consumer.accept(GeneratorsBlocks.SATURATING_CONDENSER.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());
	}

	public static class TurbineWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected IntSliderWithButtons rotorsWidget;
		protected IntSliderWithButtons ventsWidget;
		protected IntSliderWithButtons condensersWidget;
		protected IntSliderWithButtons valvesWidget;

		private boolean needMoreVents;

		public TurbineWidget()
		{
			this.widthWidget.setTranslationKey("text.jei_mekanism_multiblocks.specs.width_length");
		}

		@Override
		protected IntSliderWidget createDimensionSlider(int index, int min, int max)
		{
			if (index == 0)
			{
				return new Mod2IntSliderWidget(0, 0, 0, 0, TextComponent.EMPTY, min, min, max, 0);
			}

			return super.createDimensionSlider(index, min, max);
		}

		@Override
		protected boolean isUseDimensionWidget(IntSliderWithButtons widget)
		{
			if (widget == this.lengthWidget)
			{
				return false;
			}

			return super.isUseDimensionWidget(widget);
		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslatableComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useStructuralGlassCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			consumer.accept(this.rotorsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.rotors", 0, 1, 0));
			this.rotorsWidget.getSlider().addValueChangeHanlder(this::onRotorsChanged);
			consumer.accept(this.ventsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.vents", 0, 1, 0));
			this.ventsWidget.getSlider().addValueChangeHanlder(this::onVentsChanged);
			consumer.accept(this.condensersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.condensers", 0, 0, 0));
			this.condensersWidget.getSlider().addValueChangeHanlder(this::onCondensersChanged);
			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
			this.valvesWidget.getSlider().addValueChangeHanlder(this::onValvesChanged);

			this.updateRotorsSliderLimit();
		}

		@Override
		protected void onDimensionWidthChanged(int width)
		{
			width += width % 2 - 1;
			IntSliderWidget widthSlider = this.widthWidget.getSlider();
			widthSlider.setValue(width);

			super.onDimensionWidthChanged(width);

			IntSliderWidget lengthSlider = this.lengthWidget.getSlider();
			lengthSlider.setMinValue(width);
			lengthSlider.setMaxValue(width);
			lengthSlider.setValue(width);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateRotorsSliderLimit();
			this.setRotorCount(this.rotorsWidget.getSlider().getMaxValue());
			this.setVentCount(this.getClampedMaxVentCount(this.getRotorCount()));
			this.setCondenserCount(this.getClampedMaxCondenserCount(this.getRotorCount(), this.getVentCount()));
		}

		public void updateRotorsSliderLimit()
		{
			Vec3i inner = this.getDimensionInner();
			int innerRadius = (inner.getX() - 1) / 2;

			IntSliderWidget rotorsSlider = this.rotorsWidget.getSlider();
			int rotors = rotorsSlider.getValue();
			rotorsSlider.setMaxValue(Math.min((innerRadius + 1) * 4 - 3, inner.getY() - 2));
			rotorsSlider.setValue(rotors);

			this.updateVentsSliderLimit();
		}

		protected void onRotorsChanged(int rotors)
		{
			this.updateVentsSliderLimit();
			this.setVentCount(this.getClampedMaxVentCount(rotors));
			this.setCondenserCount(this.getClampedMaxCondenserCount(rotors, this.getVentCount()));
			this.markNeedUpdate();
		}

		public void updateVentsSliderLimit()
		{
			IntSliderWidget ventsSlider = this.ventsWidget.getSlider();
			int vents = ventsSlider.getValue();
			ventsSlider.setMaxValue(this.getClampedMaxVentCount(this.getRotorCount()));
			ventsSlider.setValue(vents);

			this.updateCondensersSliderLimit();
			this.updateValvesSliderLimit();
		}

		protected void onVentsChanged(int vents)
		{
			this.updateCondensersSliderLimit();
			this.updateValvesSliderLimit();
			this.markNeedUpdate();
		}

		public void updateCondensersSliderLimit()
		{
			IntSliderWidget condensersSlider = this.condensersWidget.getSlider();
			int condensers = condensersSlider.getValue();
			condensersSlider.setMaxValue(this.getClampedMaxCondenserCount(this.getRotorCount(), this.getVentCount()));
			condensersSlider.setValue(condensers);
		}

		protected void onCondensersChanged(int condensers)
		{
			this.markNeedUpdate();
		}

		public void updateValvesSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getValue();
			valvesSlider.setMaxValue(this.getSideBlocks() - this.getVentCount());
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
			int rotors = this.getRotorCount();
			int blades = this.getBladeCount(rotors);
			int lowerVolume = this.getLowerVolume(rotors);

			int vents = this.getVentCount();
			sides -= vents;

			int valves = this.getValveCount();
			sides -= valves;

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

			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_CASING, casing));
			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_VALVE, valves));
			CostWidget vent = consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_VENT, vents));

			if (this.needMoreVents)
			{
				vent.setFGColor(0xFF8000);
				vent.setHeadTooltip(//
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.value_limited", new TranslatableComponent("text.jei_mekanism_multiblocks.result.max_flow_rate")).withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.TURBINE_VENT.getTextComponent()).withStyle(ChatFormatting.RED));
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.ROTATIONAL_COMPLEX));
			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_ROTOR, rotors));
			consumer.accept(new ItemStack(GeneratorsItems.TURBINE_BLADE, blades));
			consumer.accept(new ItemStack(MekanismBlocks.PRESSURE_DISPERSER, this.getDisperserCount()));
			consumer.accept(new ItemStack(GeneratorsBlocks.ELECTROMAGNETIC_COIL, this.getNeededCoilCount(blades)));
			CostWidget maxWaterOutputWidget = consumer.accept(new ItemStack(GeneratorsBlocks.SATURATING_CONDENSER, this.getCondenserCount()));

			int maxFlow = MathUtils.clampToInt(this.getMaxFlowRateClamped(lowerVolume, vents));

			if (maxFlow > this.getMaxWaterOutput())
			{
				maxWaterOutputWidget.setFGColor(0xFF8000);
				maxWaterOutputWidget.setHeadTooltip(//
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.water_will_losing").withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.SATURATING_CONDENSER.getTextComponent()).withStyle(ChatFormatting.RED));

			}

			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int volume = this.getDimensionVolume();
			int rotors = this.getRotorCount();
			int lowerVolume = this.getLowerVolume(rotors);
			int blades = this.getBladeCount(rotors);
			int vents = this.getVentCount();

			FloatingLong maxProduction = this.getMaxProduction(lowerVolume, blades, vents);
			int maxFlow = MathUtils.clampToInt(this.getMaxFlowRateClamped(lowerVolume, vents));
			long maxWaterOutput = getMaxWaterOutput();
			long steamTank = this.getSteamTank(lowerVolume);
			long energyCapacity = this.getEnergyCapacity(volume);

			FloatingLong productionPerFlow = maxProduction.divide(maxFlow);
			TranslatableComponent productionPerFlowTooltip = new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.production_per_flow", new TranslatableComponent("%1$s/%2$s", EnergyDisplay.of(productionPerFlow).getTextComponent(), "mB"));

			ResultWidget maxProductionWidget = new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.max_production"), new TranslatableComponent("%s/t", EnergyDisplay.of(maxProduction).getTextComponent()));
			maxProductionWidget.setTooltip(productionPerFlowTooltip);

			consumer.accept(maxProductionWidget);
			ResultWidget maxFlowRateWidget = new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.max_flow_rate"), VolumeTextHelper.formatMBt(maxFlow));
			consumer.accept(maxFlowRateWidget);

			this.needMoreVents = vents < this.getClampedMaxVentCount(this.getRotorCount());

			if (this.needMoreVents)
			{
				maxFlowRateWidget.getValueLabel().setFGColor(0xFF8000);
				maxFlowRateWidget.setTooltip(//
						productionPerFlowTooltip, new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.limited").withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.TURBINE_VENT.getTextComponent()).withStyle(ChatFormatting.RED));
			}
			else
			{
				maxFlowRateWidget.setTooltip(productionPerFlowTooltip);
			}

			ResultWidget maxWaterOutputWidget = new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.max_water_output"), VolumeTextHelper.formatMBt(maxWaterOutput));
			consumer.accept(maxWaterOutputWidget);

			if (maxFlow > maxWaterOutput)
			{
				maxWaterOutputWidget.getValueLabel().setFGColor(0xFF8000);
				maxWaterOutputWidget.setTooltip(//
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.water_will_losing").withStyle(ChatFormatting.RED), //
						new TranslatableComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.SATURATING_CONDENSER.getTextComponent()).withStyle(ChatFormatting.RED));
			}

			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(steamTank)));
			consumer.accept(new ResultWidget(new TranslatableComponent("text.jei_mekanism_multiblocks.result.energy_capacity"), EnergyDisplay.of(FloatingLong.create(energyCapacity)).getTextComponent()));
		}

		public long getSteamTank(int lowerVolume)
		{
			return lowerVolume * TurbineMultiblockData.GAS_PER_TANK;
		}

		public long getEnergyCapacity(int volume)
		{
			return volume * 16_000_000L;
		}

		public int getClampedMaxVentCount(int rotorCount)
		{
			int unclamped = this.getNeededVentCountUnclamped(this.getLowerVolume(rotorCount));
			int upperSideBlocks = this.getUpperSideBlocks(rotorCount);
			return Math.min(unclamped, upperSideBlocks);
		}

		public int getClampedMaxCondenserCount(int rotorCount, int ventCount)
		{
			int coils = this.getNeededCoilCount(this.getBladeCount(rotorCount));
			int lowerVolume = this.getLowerVolume(rotorCount);
			double maxFlowRate = this.getMaxFlowRateClamped(lowerVolume, ventCount);
			int unclampedCondensers = Mth.ceil(maxFlowRate / MekanismGeneratorsConfig.generators.condenserRate.get());
			return Math.min(unclampedCondensers, this.getUpperInnerVolume(rotorCount) - coils);
		}

		public int getLowerSideBlocks(int rotorCount)
		{
			Vec3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			return innerSquare + (inner.getX() * 2 + inner.getZ() * 2) * rotorCount;
		}

		public int getUpperSideBlocks(int rotorCount)
		{
			Vec3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			int upperHeight = this.getUpperHeight(rotorCount);
			return innerSquare + (inner.getX() * 2 + inner.getZ() * 2) * upperHeight;
		}

		public int getUpperHeight(int rotorCount)
		{
			return this.getDimensionInner().getY() - rotorCount;
		}

		public int getUpperInnerVolume(int rotorCount)
		{
			Vec3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			int upperHeight = this.getUpperHeight(rotorCount);
			return innerSquare * (upperHeight - 1);
		}

		public int getNeededVentCountUnclamped(int lowerVolume)
		{
			double flowRate = this.getMaxFlowRateUnclamped(lowerVolume);
			return Mth.ceil(flowRate / MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
		}

		public int getNeededCoilCount(int bladeCount)
		{
			return Mth.ceil((double) bladeCount / MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
		}

		public double getMaxFlowRateUnclamped(int lowerVolume)
		{
			return lowerVolume * this.getDisperserCount() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get();
		}

		public double getMaxFlowRateClamped(int lowerVolume, int vents)
		{
			double unclamped = this.getMaxFlowRateUnclamped(lowerVolume);
			return Math.min(unclamped, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
		}

		public long getMaxWaterOutput()
		{
			return (long) this.getCondenserCount() * MekanismGeneratorsConfig.generators.condenserRate.get();
		}

		public FloatingLong getMaxProduction(int lowerVolume, int blades, int vents)
		{
			double flowRate = this.getMaxFlowRateClamped(lowerVolume, vents);

			if (flowRate > 0.0D)
			{
				int coils = this.getNeededCoilCount(lowerVolume);
				FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES).multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
				return energyMultiplier.multiply(flowRate);
			}
			else
			{
				return FloatingLong.ZERO;
			}

		}

		public int getLowerVolume(int rotorCount)
		{
			Vec3i outer = this.getDimension();
			return outer.getX() * outer.getZ() * rotorCount;
		}

		public int getDisperserCount()
		{
			Vec3i inner = this.getDimensionInner();
			return (inner.getX() * inner.getZ()) - 1;
		}

		public int getValveCount()
		{
			return this.valvesWidget.getSlider().getValue();
		}

		public void setValveCount(int valveCount)
		{
			this.valvesWidget.getSlider().setValue(valveCount);
		}

		public int getRotorCount()
		{
			return this.rotorsWidget.getSlider().getValue();
		}

		public int getBladeCount(int rotorCount)
		{
			return rotorCount * 2;
		}

		public void setRotorCount(int rotorCount)
		{
			this.rotorsWidget.getSlider().setValue(rotorCount);
		}

		public int getCondenserCount()
		{
			return this.condensersWidget.getSlider().getValue();
		}

		public void setCondenserCount(int condenserCount)
		{
			this.condensersWidget.getSlider().setValue(condenserCount);
		}

		public int getVentCount()
		{
			return this.ventsWidget.getSlider().getValue();
		}

		public void setVentCount(int ventCount)
		{
			this.ventsWidget.getSlider().setValue(ventCount);
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
			return 5;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 17;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return this.getDimensionWidthMin();
		}

		@Override
		public int getDimensionLengthMax()
		{
			return this.getDimensionWidthMax();
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 5;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

	}

}
