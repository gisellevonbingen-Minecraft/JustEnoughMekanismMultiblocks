package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.TooltipHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.FissionLayoutScreen;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LongSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LongSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.chemical.gas.attribute.GasAttributes.Coolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismGases.Coolants;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class FissionReactorCategory extends MultiblockCategory<FissionReactorCategory.FissionReactorCategoryWidget>
{
	public FissionReactorCategory(IGuiHelper helper)
	{
		super(helper, MekanismGenerators.rl("fission_reactor"), FissionReactorCategoryWidget.class, GeneratorsLang.FISSION_REACTOR.translate(), GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_CASING.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_PORT.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY.getItemStack());
		consumer.accept(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getItemStack());
		consumer.accept(GeneratorsBlocks.REACTOR_GLASS.getItemStack());
	}

	public static class FissionReactorCategoryWidget extends MultiblockWidget
	{
		public static final double WATER_CONDUCTIVITY = 0.5D;

		protected Layout currentLayout;
		protected Layout advancedLayout;
		protected int fuelRodAssemblies;
		protected int controlRodAssemblies;
		protected int surfaceArea;
		protected double boilEfficiency;

		protected CheckBoxWidget advancedCheckBox;
		protected ButtonWidget layoutButton;

		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;
		protected LongSliderWithButtons burnRateWidget;

		public FissionReactorCategoryWidget()
		{
			this.currentLayout = this.createEmptyLayout();
			this.advancedLayout = this.createEmptyLayout();
			this.advancedLayout.resetPillars();
			this.onLayoutChanged();
		}

		public Layout createEmptyLayout()
		{
			return new Layout(//
					this.getDimensionWidthMin(), this.getDimensionWidthMax(), //
					this.getDimensionLengthMin(), this.getDimensionLengthMax(), //
					this.getDimensionHeightMin(), this.getDimensionHeightMax()//
			);
		}

		@Override
		protected void createSpecDimension()
		{
			this.advancedCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.advanced"), false);
			this.advancedCheckBox.addSelectedChangedHandler(this::onAdvancedChanged);
			this.configsList.addChild(this.advancedCheckBox);

			this.layoutButton = new ButtonWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.layout"));
			this.layoutButton.addPressHandler(this::onLayoutButtonClick);
			this.configsList.addChild(this.layoutButton);

			super.createSpecDimension();

			this.updateDimensionVisible();
		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 4, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);
			consumer.accept(this.burnRateWidget = new LongSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.burn_rate", 0, 0, 0));
			this.burnRateWidget.getSlider().addValueChangeHanlder(this::onBurnRateChanged);

			this.updatePortsSliderLimit();
			this.updateBurnRateSliderLimit();
			this.setBurnRate(this.getMaxBurnRate());
		}

		@Override
		public void load(CompoundTag tag)
		{
			this.setAdvanced(tag.getBoolean("Advanced"));

			if (tag.contains("AdvancedLayout"))
			{
				this.advancedLayout.load(tag.getCompound("AdvancedLayout"));
			}

			this.setAdvancedLayout(this.advancedLayout);

			super.load(tag);

			this.setPortCount(tag.getInt("PortCount"));
			this.setLogicAdapterCount(tag.getInt("LogicAdapterCount"));
			this.setBurnRate(tag.getLong("BurnRate"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			tag.putBoolean("Advanced", this.isAdvanced());
			tag.put("AdvancedLayout", this.advancedLayout.save());

			super.save(tag);

			tag.putInt("PortCount", this.getPortCount());
			tag.putInt("LogicAdapterCount", this.getLogicAdapterCount());
			tag.putLong("BurnRate", this.getBurnRate());
		}

		protected void onAdvancedChanged(boolean advanced)
		{
			this.updateDimensionVisible();

			if (this.isAdvanced())
			{
				this.currentLayout = this.advancedLayout.clone();
				this.onLayoutChanged();
			}
			else
			{
				this.onDimensionChanged();
			}

		}

		public void updateDimensionVisible()
		{
			boolean advanced = this.advancedCheckBox.isSelected();
			this.configsList.setVisible(this.widthWidget, !advanced);
			this.configsList.setVisible(this.heightWidget, !advanced);
			this.configsList.setVisible(this.lengthWidget, !advanced);

			this.configsList.setVisible(this.layoutButton, advanced);
		}

		protected void onLayoutButtonClick(AbstractButton button)
		{
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.pushGuiLayer(new FissionLayoutScreen(Component.empty(), this));
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			if (!this.isAdvanced())
			{
				this.currentLayout.setWidth(this.getDimensionWidth());
				this.currentLayout.setLength(this.getDimensionLength());
				this.currentLayout.setHeight(this.getDimensionHeight());
				this.currentLayout.resetPillars();
				this.onLayoutChanged();
			}

		}

		protected void onLayoutChanged()
		{
			this.fuelRodAssemblies = 0;
			this.controlRodAssemblies = 0;
			this.surfaceArea = 0;
			this.boilEfficiency = 0.0D;

			int pillarMaximum = this.currentLayout.getPillarMax();
			boolean[][][] set = new boolean[this.currentLayout.getHeight()][this.currentLayout.getLength()][this.currentLayout.getWidth()];

			for (int z = 1; z < this.currentLayout.getLength() - 1; z++)
			{
				for (int x = 1; x < this.currentLayout.getWidth() - 1; x++)
				{
					int pillar = Math.min(this.currentLayout.pillars[z][x], pillarMaximum);
					this.fuelRodAssemblies += pillar;

					if (pillar > 0)
					{
						this.controlRodAssemblies++;

						for (int y = 0; y < pillar; y++)
						{
							for (Direction direction : EnumUtils.DIRECTIONS)
							{
								if (set[1 + y + direction.getStepY()][z + direction.getStepZ()][x + direction.getStepX()])
								{
									this.surfaceArea -= 2;
								}

							}

							this.surfaceArea += 6;
							set[1 + y][z][x] = true;
						}

					}

				}

			}

			if (this.fuelRodAssemblies == 0)
			{
				this.boilEfficiency = 1.0D;
			}
			else
			{
				double avgSurfaceArea = this.surfaceArea / (double) this.fuelRodAssemblies;
				this.boilEfficiency = Math.min(1, avgSurfaceArea / MekanismGeneratorsConfig.generators.fissionSurfaceAreaTarget.get());
			}

			this.updatePortsSliderLimit();
			this.updateBurnRateSliderLimit();

			this.setBurnRate(this.getMaxBurnRate());

			this.markNeedUpdate();
		}

		public void updatePortsSliderLimit()
		{
			IntSliderWidget portsSlider = this.portsWidget.getSlider();
			int valves = portsSlider.getValue();
			portsSlider.setMaxValue(this.getSideBlocks());
			portsSlider.setValue(valves);

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

		public void updateBurnRateSliderLimit()
		{
			LongSliderWidget burnRateSlider = this.burnRateWidget.getSlider();
			long burnRate = burnRateSlider.getValue();
			burnRateSlider.setMaxValue(this.getMaxBurnRate());
			burnRateSlider.setValue(burnRate);
		}

		protected void onBurnRateChanged(long burnRate)
		{
			this.markNeedUpdate();
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

			int casings = 0;
			int glasses = 0;

			if (this.isUseGlass())
			{
				casings = corners;
				glasses = sides;
			}
			else
			{
				casings = corners + sides;
				glasses = 0;
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_CASING, casings));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, this.getFissionFuelAssemblyCount()));
			consumer.accept(new ItemStack(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY, this.getControlRodAssemblyCount()));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			long coolantCapacity = this.getCooledCoolantCapacity();
			long heatedCoolantCapacity = this.getHeatedCoolantCapacity();
			long maxBurnRate = this.getMaxBurnRate();
			long fuelCapacity = this.getFuelCapacity();
			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.max_burn_rate"), VolumeTextHelper.formatMBt(maxBurnRate)));
			this.createStableTempWidgets(consumer);
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(coolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_FUEL_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(heatedCoolantCapacity)));
			consumer.accept(new ResultWidget(GeneratorsLang.FISSION_WASTE_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
		}

		public void createStableTempWidgets(Consumer<AbstractWidget> consumer)
		{
			this.createStableTempWidget(consumer, new FluidStack(Fluids.WATER, 1).getDisplayName(), WATER_CONDUCTIVITY, HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
			this.createStableTempWidget(consumer, MekanismGases.SODIUM.getTextComponent(), Coolants.SODIUM_COOLANT);
		}

		public void createStableTempWidget(Consumer<AbstractWidget> consumer, Component with, Coolant coolant)
		{
			this.createStableTempWidget(consumer, with, coolant.getConductivity(), coolant.getThermalEnthalpy());
		}

		public void createStableTempWidget(Consumer<AbstractWidget> consumer, Component with, double conductivity, double thermalEnthalpy)
		{
			double stableTemp = this.getCoolingStableTemp(conductivity, thermalEnthalpy);
			ResultWidget tempWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.temp_with", with), MekanismUtils.getTemperatureDisplay(stableTemp, TemperatureUnit.KELVIN, true));
			consumer.accept(tempWidget);

			boolean warning = false;

			if (Double.isInfinite(stableTemp))
			{
				warning = true;
				tempWidget.getValueLabel().setFGColor(0xFF0000);
			}
			else if (stableTemp >= FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE)
			{
				warning = true;
				double ratio = Mth.inverseLerp(stableTemp, FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE, FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
				int g = (int) Mth.clampedLerp(255, 0, ratio);
				tempWidget.getValueLabel().setFGColor(0xFF0000 + g * 256);
			}

			long toBurn = this.getBurnRate();
			Component burnRateTooltip = Component.translatable("text.jei_mekanism_multiblocks.tooltip.when_burn_rate", VolumeTextHelper.formatMBt(toBurn));

			if (warning)
			{
				tempWidget.getValueLabel().setMessage(Component.translatable("※ %s", tempWidget.getValueLabel().getMessage()));
				tempWidget.setTooltip(TooltipHelper.createMessageOnly(//
						burnRateTooltip, //
						Component.translatable("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(ChatFormatting.RED), //
						Component.translatable("text.jei_mekanism_multiblocks.tooltip.reactor_will_damage").withStyle(ChatFormatting.RED)));
			}
			else
			{
				tempWidget.setTooltip(TooltipHelper.createMessageOnly(burnRateTooltip));
			}

			long heatedCoolant = this.getHeatedCoolant(stableTemp, conductivity, thermalEnthalpy);
			ResultWidget heatingRateWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.heating_rate_with", with), VolumeTextHelper.formatMBt(heatedCoolant));
			heatingRateWidget.setTooltip(TooltipHelper.createMessageOnly(burnRateTooltip));
			consumer.accept(heatingRateWidget);
		}

		private void simulateTemp(double conductivity)
		{
			long coolantCapacity = this.getCooledCoolantCapacity();
			long toBurn = this.getBurnRate();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = this.getHeatCapacity();
			double boilEfficiency = this.getBoilEfficiency();

			double heat = HeatAPI.AMBIENT_TEMP * heatCapacity;
			double prevHeat = 0.0D;

			for (int i = 0; i < 100; i++)
			{
				double temp = heat / heatCapacity;

				heat += burnHeat;

				double boilHeat = boilEfficiency * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacity;
				double caseCoolantHeat = boilHeat * conductivity;
				long coolantHeated = (int) (HeatUtils.getSteamEnergyEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy());
				coolantHeated = Math.max(0, Math.min(coolantHeated, coolantCapacity));

				if (coolantHeated > 0)
				{
					caseCoolantHeat = coolantHeated * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
					heat -= caseCoolantHeat;
				}

				System.out.println("Temp: " + (heat / heatCapacity));

				if (prevHeat == heat)
				{
					System.out.println("Stabled");
					break;
				}

				prevHeat = heat;
			}

		}

		public long getHeatedCoolant(double temp, double conductivity, double thermalEnthalpy)
		{
			double boilHeat = this.getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * this.getHeatCapacity();
			double caseCoolantHeat = boilHeat * conductivity;
			long coolantHeated = MathUtils.clampToLong(caseCoolantHeat / thermalEnthalpy);
			return Math.max(0, Math.min(coolantHeated, this.getCooledCoolantCapacity()));
		}

		public long getCooledCoolantCapacity()
		{
			return this.getDimensionVolume() * MekanismGeneratorsConfig.generators.fissionCooledCoolantPerTank.get();
		}

		public long getHeatedCoolantCapacity()
		{
			return this.getDimensionVolume() * MekanismGeneratorsConfig.generators.fissionHeatedCoolantPerTank.get();
		}

		public long getMaxBurnRate()
		{
			return this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
		}

		public long getFuelCapacity()
		{
			return this.getFissionFuelAssemblyCount() * MekanismGeneratorsConfig.generators.maxFuelPerAssembly.get();
		}

		public double getHeatCapacity()
		{
			return MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * this.getDimensionCasingBlocks();
		}

		public double getCoolingStableTemp(double conductivity, double thermalEnthalpy)
		{
			long toBurn = this.getBurnRate();

			if (toBurn <= 0L)
			{
				return HeatAPI.AMBIENT_TEMP;
			}

			long coolantCapacity = this.getCooledCoolantCapacity();
			double burnHeat = toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
			double heatCapacity = this.getHeatCapacity();

			double coolantHeated = burnHeat / thermalEnthalpy;

			if (coolantHeated > coolantCapacity)
			{
				return Double.POSITIVE_INFINITY;
			}

			double boilHeat = burnHeat / conductivity;
			return boilHeat / (heatCapacity * this.getBoilEfficiency()) + HeatUtils.BASE_BOIL_TEMP;
		}

		public Layout getCurrentLayout()
		{
			return this.currentLayout.clone();
		}

		public boolean isAdvanced()
		{
			return this.advancedCheckBox.isSelected();
		}

		public void setAdvanced(boolean advanced)
		{
			this.advancedCheckBox.setSelected(advanced);
		}

		public Layout getAdvancedLayout()
		{
			return this.advancedLayout.clone();
		}

		public void setAdvancedLayout(Layout layout)
		{
			this.advancedLayout = layout.clone();
			this.advancedLayout.setWidth(Mth.clamp(this.advancedLayout.getWidth(), this.getDimensionWidthMin(), this.getDimensionWidthMax()));
			this.advancedLayout.setLength(Mth.clamp(this.advancedLayout.getLength(), this.getDimensionLengthMin(), this.getDimensionLengthMax()));
			this.advancedLayout.setHeight(Mth.clamp(this.advancedLayout.getHeight(), this.getDimensionHeightMin(), this.getDimensionHeightMax()));

			if (this.isAdvanced())
			{
				this.currentLayout = this.advancedLayout.clone();
				this.onLayoutChanged();
			}

		}

		public int getControlRodAssemblyCount()
		{
			return this.controlRodAssemblies;
		}

		public int getFissionFuelAssemblyCount()
		{
			return this.fuelRodAssemblies;
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

		public long getBurnRate()
		{
			return this.burnRateWidget.getSlider().getValue();
		}

		public void setBurnRate(long burnRate)
		{
			this.burnRateWidget.getSlider().setValue(burnRate);
		}

		public double getBoilEfficiency()
		{
			return this.boilEfficiency;
		}

		@Override
		public int getDimensionWidth()
		{
			if (this.isAdvanced())
			{
				return this.advancedLayout.getWidth();
			}

			return super.getDimensionWidth();
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
		public int getDimensionLength()
		{
			if (this.isAdvanced())
			{
				return this.advancedLayout.getLength();
			}

			return super.getDimensionLength();
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
		public int getDimensionHeight()
		{
			if (this.isAdvanced())
			{
				return this.advancedLayout.getHeight();
			}

			return super.getDimensionHeight();
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 4;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

		@Override
		public Block getGlassBlock()
		{
			return GeneratorsBlocks.REACTOR_GLASS.getBlock();
		}

		public static class Layout
		{
			private final int widthMin;
			private final int widthMax;
			private final int lengthMin;
			private final int lengthMax;
			private final int heightMin;
			private final int heightMax;

			private int width;
			private int length;
			private int height;
			private int pillarMax;
			private int[][] pillars = new int[0][0];

			public Layout(int widthMin, int widthMax, int lengthMin, int lengthMax, int heightMin, int heightMax)
			{
				this.widthMin = widthMin;
				this.widthMax = widthMax;
				this.lengthMin = lengthMin;
				this.lengthMax = lengthMax;
				this.heightMin = heightMin;
				this.heightMax = heightMax;

				this.setWidth(widthMin);
				this.setLength(lengthMin);
				this.setHeight(heightMin);
				this.pillars = new int[lengthMax][widthMax];

				this.resetPillars();
			}

			public Layout(Layout other)
			{
				this.widthMin = other.widthMin;
				this.widthMax = other.widthMax;
				this.lengthMin = other.lengthMin;
				this.lengthMax = other.lengthMax;
				this.heightMin = other.heightMin;
				this.heightMax = other.heightMax;

				this.width = other.width;
				this.length = other.length;
				this.height = other.height;
				this.pillarMax = other.pillarMax;
				this.pillars = new int[other.pillars.length][];

				for (int z = 0; z < other.pillars.length; z++)
				{
					this.pillars[z] = new int[other.pillars[z].length];

					for (int x = 0; x < other.pillars[z].length; x++)
					{
						this.pillars[z][x] = other.pillars[z][x];
					}

				}

			}

			@Override
			public Layout clone()
			{
				return new Layout(this);
			}

			public boolean canPillar(int x, int z)
			{
				return (0 < x && x < this.width - 1) && (0 < z && z < this.length - 1);
			}

			public int getPillarMax()
			{
				return this.pillarMax;
			}

			public int getPillar(int x, int z)
			{
				if (this.canPillar(x, z))
				{
					return Mth.clamp(this.pillars[z][x], 0, this.getPillarMax());
				}

				return 0;
			}

			public void setPillar(int x, int z, int pillar)
			{
				if (this.canPillar(x, z))
				{
					this.pillars[z][x] = Mth.clamp(pillar, 0, this.getPillarMax());
				}

			}

			public void clearPillars()
			{
				for (int z = 0; z < this.pillars.length; z++)
				{
					for (int x = 0; x < this.pillars[z].length; x++)
					{
						this.pillars[z][x] = 0;
					}

				}

			}

			public void resetPillars()
			{
				this.clearPillars();

				int pillarMaximum = this.getHeightMax();

				for (int z = 1; z < this.pillars.length - 1; z++)
				{
					boolean b1 = z % 2 == 0;

					for (int x = 1; x < this.pillars[z].length - 1; x++)
					{
						this.pillars[z][x] = b1 == (x % 2 == 0) ? pillarMaximum : 0;
					}

				}

			}

			public void load(CompoundTag tag)
			{
				this.width = tag.getInt("width");
				this.length = tag.getInt("length");
				this.height = tag.getInt("height");
				int[] pillars = tag.getIntArray("pillars");
				int pillarsWidth = Math.min(tag.getInt("pillarsWidth"), this.widthMax);
				int pillarsLength = Math.min(tag.getInt("pillarsLength"), this.lengthMax);
				this.clearPillars();

				for (int z = 0; z < pillarsLength; z++)
				{
					for (int x = 0; x < pillarsWidth; x++)
					{
						this.pillars[z][x] = pillars[z * pillarsWidth + x];
					}

				}

			}

			public CompoundTag save()
			{
				int pillarsWidth = this.widthMax;
				int pillarsLength = this.lengthMax;
				int[] pillars = new int[pillarsLength * pillarsWidth];

				for (int z = 0; z < pillarsLength; z++)
				{
					for (int x = 0; x < pillarsWidth; x++)
					{
						pillars[z * pillarsWidth + x] = this.pillars[z][x];
					}

				}

				CompoundTag tag = new CompoundTag();
				tag.putInt("width", this.width);
				tag.putInt("length", this.length);
				tag.putInt("height", this.height);
				tag.putIntArray("pillars", pillars);
				tag.putInt("pillarsWidth", pillarsWidth);
				tag.putInt("pillarsLength", pillarsLength);
				return tag;
			}

			public int getWidthMin()
			{
				return this.widthMin;
			}

			public int getWidthMax()
			{
				return this.widthMax;
			}

			public int getLengthMin()
			{
				return this.lengthMin;
			}

			public int getLengthMax()
			{
				return this.lengthMax;
			}

			public int getHeightMin()
			{
				return this.heightMin;
			}

			public int getHeightMax()
			{
				return this.heightMax;
			}

			public int getWidth()
			{
				return this.width;
			}

			public void setWidth(int width)
			{
				this.width = Mth.clamp(width, this.widthMin, this.widthMax);
			}

			public int getLength()
			{
				return this.length;
			}

			public void setLength(int length)
			{
				this.length = Mth.clamp(length, this.lengthMin, this.lengthMax);
			}

			public int getHeight()
			{
				return this.height;
			}

			public void setHeight(int height)
			{
				this.height = Mth.clamp(height, this.heightMin, this.heightMax);
				this.pillarMax = this.height - 3;
			}

		}

	}

}
