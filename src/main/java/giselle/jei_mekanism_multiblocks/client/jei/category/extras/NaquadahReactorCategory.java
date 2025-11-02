package giselle.jei_mekanism_multiblocks.client.jei.category.extras;

import java.util.List;
import java.util.function.Consumer;

import com.jerry.generator_extras.common.ExtraGenLang;
import com.jerry.generator_extras.common.config.GenLoadConfig;
import com.jerry.generator_extras.common.genregistry.ExtraGenBlocks;
import com.jerry.generator_extras.common.genregistry.ExtraGenItem;
import com.jerry.mekanism_extras.MekanismExtras;
import com.jerry.mekanism_extras.common.ExtraTag;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class NaquadahReactorCategory extends MultiblockCategory<NaquadahReactorCategory.NaquadahReactor>
{
	public NaquadahReactorCategory(IGuiHelper helper)
	{
		super(helper, MekanismExtras.rl("naquadah_reactor"), NaquadahReactor.class, ExtraGenLang.NAQUADAH_REACTOR.translate(), ExtraGenBlocks.NAQUADAH_REACTOR_CONTROLLER.getItemStack());
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(ExtraGenBlocks.NAQUADAH_REACTOR_CONTROLLER.getItemStack());
		consumer.accept(ExtraGenBlocks.NAQUADAH_REACTOR_CASING.getItemStack());
		consumer.accept(ExtraGenBlocks.NAQUADAH_REACTOR_PORT.getItemStack());
		consumer.accept(ExtraGenBlocks.NAQUADAH_REACTOR_LOGIC_ADAPTER.getItemStack());
		consumer.accept(ExtraGenBlocks.LEAD_COATED_LASER_FOCUS_MATRIX.getItemStack());
		consumer.accept(ExtraGenBlocks.LEAD_COATED_GLASS.getItemStack());

		List<Gas> fusionFuelGases = ChemicalTags.GAS.getManager().get().getTag(ExtraTag.Gases.NAQUADAH_URANIUM_FUEL).stream().toList();
		long capacity = GenLoadConfig.generatorConfig.hohlraumMaxGas.get();

		for (Gas gas : fusionFuelGases)
		{
			consumer.accept(ChemicalUtil.getFilledVariant(new ItemStack(ExtraGenItem.HOHLRAUM.get()), capacity, gas));
		}

	}

	public static class NaquadahReactor extends MultiblockWidget
	{
		protected CheckBoxWidget waterCooledCheckBox;
		protected IntSliderWithButtons portsWidget;
		protected IntSliderWithButtons logicAdaptersWidget;

		public NaquadahReactor()
		{

		}

		@Override
		protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.waterCooledCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.water_cooled"), false));
			this.waterCooledCheckBox.addSelectedChangedHandler(this::onWaterCooledChanged);
			consumer.accept(this.portsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.ports", 0, 0, 0));
			this.portsWidget.getSlider().addValueChangeHanlder(this::onPortsChanged);
			consumer.accept(this.logicAdaptersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.logic_adapters", 0, 0, 0));
			this.logicAdaptersWidget.getSlider().addValueChangeHanlder(this::onLogicAdaptersChanged);

			this.updatePortsSliderLimit();
		}

		@Override
		public void load(CompoundTag tag)
		{
			super.load(tag);

			this.setWaterCooled(tag.getBoolean("WaterCooled"));
			this.setPortCount(tag.getInt("PortCount"));
			this.setLogicAdapterCount(tag.getInt("LogicAdapterCount"));
		}

		@Override
		public void save(CompoundTag tag)
		{
			super.save(tag);

			tag.putBoolean("WaterCooled", this.isWaterCooled());
			tag.putInt("PortCount", this.getPortCount());
			tag.putInt("LogicAdapterCount", this.getLogicAdapterCount());
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
			// 108 Totals
			return (24 + 16 + 8 + 4) * 2 + 4;
		}

		@Override
		public int getSideBlocks()
		{
			// 222 Totals
			// 1 Controller
			// 1 Laser Focus Matrix
			return (37 * 6) - 2;
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

		protected void onWaterCooledChanged(boolean waterCooled)
		{
			this.updatePortsSliderLimit();

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

			int frames = 0;
			int glasses = 0;

			if (this.isUseGlass())
			{
				frames = corners;
				glasses = sides;
			}
			else
			{
				frames = corners + sides;
				glasses = 0;
			}

			consumer.accept(new ItemStack(ExtraGenBlocks.NAQUADAH_REACTOR_CONTROLLER));
			consumer.accept(new ItemStack(ExtraGenBlocks.NAQUADAH_REACTOR_CASING, frames));
			consumer.accept(new ItemStack(ExtraGenBlocks.NAQUADAH_REACTOR_PORT, ports));
			consumer.accept(new ItemStack(ExtraGenBlocks.NAQUADAH_REACTOR_LOGIC_ADAPTER, logicAdapter));
			consumer.accept(new ItemStack(ExtraGenBlocks.LEAD_COATED_LASER_FOCUS_MATRIX));
			consumer.accept(new ItemStack(this.getGlassBlock(), glasses));
		}

		@Override
		protected void collectResult(Consumer<AbstractWidget> consumer)
		{
			super.collectResult(consumer);

			consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.not_supported"), Component.literal("-")));
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
			return 9;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 9;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return 9;
		}

		@Override
		public int getDimensionLengthMax()
		{
			return 9;
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 9;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 9;
		}

		@Override
		public Block getGlassBlock()
		{
			return ExtraGenBlocks.LEAD_COATED_GLASS.getBlock();
		}

	}

}
