package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.List;
import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.Mod2IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidType;

public class FusionReactorCategory extends MultiblockCategory<FusionReactorCategory.FusionReactorCategoryWidget>
{
	public FusionReactorCategory(IGuiHelper helper)
	{
		super(helper, MekanismGenerators.rl("fusion_reactor"), FusionReactorCategoryWidget.class, GeneratorsLang.FUSION_REACTOR.translate(), GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_FRAME.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_PORT.getItemStack());
		consumer.accept(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(GeneratorsBlocks.LASER_FOCUS_MATRIX.getItemStack());
		consumer.accept(GeneratorsBlocks.REACTOR_GLASS.getItemStack());

		List<Gas> fusionFuelGases = ChemicalTags.GAS.getManager().get().getTag(GeneratorTags.Gases.FUSION_FUEL).stream().toList();

		if (fusionFuelGases.size() > 0)
		{
			Gas fusionFuelGas = fusionFuelGases.get(0);
			long capacity = MekanismGeneratorsConfig.generators.hohlraumMaxGas.get();
			consumer.accept(ChemicalUtil.getFilledVariant(GeneratorsItems.HOHLRAUM.getItemStack(), capacity, fusionFuelGas));
		}
		else
		{
			consumer.accept(GeneratorsItems.HOHLRAUM.getItemStack());
		}

	}

	public static class FusionReactorCategoryWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useReactorGlassCheckBox;
		protected CheckBoxWidget waterCooledCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected IntSliderWithButtons injectionRateWidget;

		public FusionReactorCategoryWidget()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useReactorGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.REACTOR_GLASS.getItemStack().getHoverName()), true));
			this.useReactorGlassCheckBox.addSelectedChangedHandler(this::onUseReactorGlassChanged);
			consumer.accept(this.waterCooledCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.water_cooled"), false));
			this.waterCooledCheckBox.addSelectedChangedHandler(this::onWaterCooledChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 0, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.injectionRateWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.injection_rate", new Mod2IntSliderWidget(0, 0, 0, 0, Component.empty(), 2, 2, FluidType.BUCKET_VOLUME, 1)));
			this.injectionRateWidget.getSlider().addValueChangeHanlder(this::onInjectionRateChanged);

			this.updatePortsSliderLimit();
			this.updateInjectionRateInfoMessage();
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updatePortsSliderLimit();
		}

		@Override
		public int getCornerBlocks()
		{
			// 36 Totals
			return 8 + 8 + 4 + 8 + 8;
		}

		@Override
		public int getSideBlocks()
		{
			// 30 Totals
			// 1 Controller
			// 1 Laser Focus Matrix
			return 5 + 4 + 12 + 4 + 5 - 2;
		}

		public void updatePortsSliderLimit()
		{
			IntSliderWidget portsSlider = this.portsWidget.getSlider();
			int minPorts = portsSlider.getMinValue();
			int ports = portsSlider.getValue();
			portsSlider.setMinValue(this.isWaterCooled() ? 4 : 2);
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setValue(ports + (portsSlider.getMinValue() - minPorts));

			this.updateLogicAdaptersSliderLimit();
		}

		public void updateLogicAdaptersSliderLimit()
		{
			IntSliderWidget adaptersSlider = this.logicAdaptersWidget.getSlider();
			int adapters = adaptersSlider.getValue();
			adaptersSlider.setMaxValue(this.getSideBlocks() - this.getPortCount());
			adaptersSlider.setValue(adapters);
		}

		protected void onPortsChanged(int ports)
		{
			this.updateLogicAdaptersSliderLimit();
			this.markNeedUpdate();
		}

		protected void onLogicAdaptersChanged(int logicAdapters)
		{
			this.markNeedUpdate();
		}

		protected void onUseReactorGlassChanged(boolean useReactorGlass)
		{
			this.markNeedUpdate();
		}

		protected void onInjectionRateChanged(int injectionRate)
		{
			this.markNeedUpdate();
			this.updateInjectionRateInfoMessage();
		}

		protected void onWaterCooledChanged(boolean waterCooled)
		{
			this.updatePortsSliderLimit();

			this.markNeedUpdate();
			this.updateInjectionRateInfoMessage();
		}

		public void updateInjectionRateInfoMessage()
		{
			if (this.isWaterCooled())
			{
				int limitedInjectionRate = Math.min(this.getInjectionRate(), FusionReactorMultiblockData.MAX_INJECTION);
				Component tooltip = Component.translatable("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				this.waterCooledCheckBox.setTooltip(tooltip);
				this.injectionRateWidget.setTooltip(tooltip);
			}
			else
			{
				this.waterCooledCheckBox.setTooltip();
				this.injectionRateWidget.setTooltip();
			}

		}

		@Override
		protected void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int ports = this.getPortCount();
			sides -= ports;
			int logicAdapter = this.getLogicAdapterCount();
			sides -= logicAdapter;

			int frames = 0;
			int reactorGlasses = 0;

			if (this.isUseReactorGlass())
			{
				frames = corners;
				reactorGlasses = sides;
			}
			else
			{
				frames = corners + sides;
				reactorGlasses = 0;
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_FRAME, frames));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(GeneratorsBlocks.LASER_FOCUS_MATRIX));
			consumer.accept(new ItemStack(GeneratorsBlocks.REACTOR_GLASS, reactorGlasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			int injectionRate = this.getInjectionRate();
			int limitedInjectionRate = Math.min(injectionRate, FusionReactorMultiblockData.MAX_INJECTION);
			long waterTank = MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get() * limitedInjectionRate;
			long steamTank = MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get() * limitedInjectionRate;
			long fuelTank = MekanismGeneratorsConfig.generators.fusionFuelCapacity.get();

			FloatingLong energyFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get();
			double casingThermalConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
			double casingTemp = energyFusionFuel.multiply(injectionRate).divide(casingThermalConductivity).doubleValue();
			long steamProduction = 0L;

			if (this.isWaterCooled())
			{
				double waterHeatingRatio = MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get();
				double wateredCasingTemp = energyFusionFuel.multiply(injectionRate).divide(casingThermalConductivity + waterHeatingRatio).doubleValue();
				double waterHeat = waterHeatingRatio * wateredCasingTemp;
				steamProduction = (long) (HeatUtils.getSteamEnergyEfficiency() * waterHeat / HeatUtils.getWaterThermalEnthalpy());
				steamProduction = Math.min(steamProduction, waterTank);

				double coolingHeat = steamProduction / HeatUtils.getSteamEnergyEfficiency() * HeatUtils.getWaterThermalEnthalpy();
				double coolingCasingTemp = coolingHeat / casingThermalConductivity;
				casingTemp -= coolingCasingTemp;
			}

			double fusionThermocoupleEfficiency = MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get();
			FloatingLong passiveGeneration = FloatingLong.create(fusionThermocoupleEfficiency * casingThermalConductivity * casingTemp);
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.passive_generation"), EnergyDisplay.of(passiveGeneration).getTextComponent()));

			if (steamProduction > 0L)
			{
				consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.steam_production"), VolumeTextHelper.formatMBt(steamProduction)));
			}

			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.fuel_tank"), VolumeTextHelper.formatMB(fuelTank)));

			if (this.isWaterCooled())
			{
				Component injectionRateTooltip = Component.translatable("text.jei_mekanism_multiblocks.tooltip.need_set_injection_rate", limitedInjectionRate);
				ResultWidget watTankWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.water_tank"), VolumeTextHelper.formatMB(waterTank));
				watTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(watTankWidget);
				ResultWidget steamTankWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMB(steamTank));
				steamTankWidget.setTooltip(injectionRateTooltip);
				consumer.accept(steamTankWidget);
			}

		}

		public int getPortCount()
		{
			return this.portsWidget.getSlider().getValue();
		}

		public void setPortCount(int portCount)
		{
			this.portsWidget.getSlider().setValue(portCount);
		}

		public int getLogicAdapterCount()
		{
			return this.logicAdaptersWidget.getSlider().getValue();
		}

		public void setLogicAdapterCount(int logicAdapterCount)
		{
			this.logicAdaptersWidget.getSlider().setValue(logicAdapterCount);
		}

		public boolean isUseReactorGlass()
		{
			return this.useReactorGlassCheckBox.isSelected();
		}

		public void setUseReactorGlass(boolean useReactorGlass)
		{
			this.useReactorGlassCheckBox.setSelected(useReactorGlass);
		}

		public int getInjectionRate()
		{
			return this.injectionRateWidget.getSlider().getValue();
		}

		public void setInjectionRate(int injectionRate)
		{
			this.injectionRateWidget.getSlider().setValue(injectionRate);
		}

		public boolean isWaterCooled()
		{
			return this.waterCooledCheckBox.isSelected();
		}

		public void setWaterCooled(boolean waterCooled)
		{
			this.waterCooledCheckBox.setSelected(waterCooled);
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 5;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 5;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 5;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 5;
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

	}

}
