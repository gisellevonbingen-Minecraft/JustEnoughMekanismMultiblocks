package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.IRecipeLogicStateListener;
import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TabButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class MultiblockWidget extends ContainerWidget
{
	private boolean initialzed = false;

	protected final ListWidget configsList;
	protected final TabButtonWidget costsButton;
	protected final TabButtonWidget resultsButton;
	protected final CostList costsList;
	protected final ListLineWidget resultsList;

	protected IntSliderWithButtons widthWidget;
	protected IntSliderWithButtons lengthWidget;
	protected IntSliderWithButtons heightWidget;

	private boolean needNotifyStateChange;

	public MultiblockWidget()
	{
		super(0, 0, 0, 0);

		LabelWidget specsLabel = this.addChild(new LabelWidget(00, 00, 100, 10, Component.translatable("text.jei_mekanism_multiblocks.specs"), TextAlignment.LEFT));
		specsLabel.setFGColor(0x404040);
		specsLabel.setShadow(false);
		this.addChild(this.configsList = new ListWidget(00, 10, 100, 110, 10));
		this.configsList.setItemsPadding(2);
		this.configsList.setItemOffset(2);

		this.addChild(this.costsButton = new TabButtonWidget(99, 0, 41, 10, Component.translatable("text.jei_mekanism_multiblocks.costs")));
		this.costsButton.addPressHandler(this::onCostsButtonClick);
		this.addChild(this.resultsButton = new TabButtonWidget(139, 0, 41, 10, Component.translatable("text.jei_mekanism_multiblocks.results")));
		this.resultsButton.addPressHandler(this::onResultsButtonClick);
		this.addChild(this.costsList = new CostList(100, 10, 80, 110, 20));
		this.costsList.setItemsTop(2);
		this.costsList.setItemOffset(1);
		this.addChild(this.resultsList = new ListLineWidget(100, 10, 80, 110, 20));
		this.resultsList.setItemsPadding(2);
		this.resultsList.setItemOffset(1);

		this.createSpecDimension();

		List<AbstractWidget> otherWidgets = new ArrayList<>();
		this.collectOtherConfigs(otherWidgets::add);

		if (otherWidgets.size() > 0)
		{
			otherWidgets.forEach(this.configsList::addChild);
		}

		this.needNotifyStateChange = true;
		this.showRightPanel(true);
	}

	private void showRightPanel(boolean costs)
	{
		boolean results = !costs;
		this.costsButton.setSelected(costs);
		this.resultsButton.setSelected(results);
		this.costsList.visible = costs;
		this.resultsList.visible = results;
	}

	private void onCostsButtonClick(AbstractButton button)
	{
		this.showRightPanel(true);
	}

	private void onResultsButtonClick(AbstractButton button)
	{
		this.showRightPanel(false);
	}

	protected void collectOtherConfigs(Consumer<AbstractWidget> consumer)
	{

	}

	private void createSpecDimension()
	{
		List<IntSliderWithButtons> widgets = new ArrayList<>();
		widgets.add(this.widthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.width", this.createDimensionSlider(0, this.getDimensionWidthMin(), this.getDimensionWidthMax())));
		this.widthWidget.getSlider().addValueChangeHanlder(this::onDimensionWidthChanged);
		widgets.add(this.lengthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.length", this.createDimensionSlider(1, this.getDimensionLengthMin(), this.getDimensionLengthMax())));
		this.lengthWidget.getSlider().addValueChangeHanlder(this::onDimensionLengthChanged);
		widgets.add(this.heightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.height", this.createDimensionSlider(2, this.getDimensionHeightMin(), this.getDimensionHeightMax())));
		this.heightWidget.getSlider().addValueChangeHanlder(this::onDimensionHeightChanged);

		List<IntSliderWithButtons> list = widgets.stream().filter(w -> this.isUseDimensionWidget(w) && w.getSlider().getMinValue() < w.getSlider().getMaxValue()).toList();

		if (list.size() > 0)
		{
			list.forEach(this.configsList::addChild);
		}

	}

	protected IntSliderWidget createDimensionSlider(int index, int min, int max)
	{
		return new IntSliderWidget(0, 0, 0, 0, Component.empty(), min, min, max);
	}

	protected boolean isUseDimensionWidget(IntSliderWithButtons widget)
	{
		return true;
	}

	protected void onDimensionWidthChanged(int width)
	{
		this.onDimensionChanged();
	}

	protected void onDimensionLengthChanged(int length)
	{
		this.onDimensionChanged();
	}

	protected void onDimensionHeightChanged(int height)
	{
		this.onDimensionChanged();
	}

	protected void onDimensionChanged()
	{
		this.markNeedUpdate();
	}

	public List<ItemStack> getCosts()
	{
		return this.costsList.getCosts();
	}

	private void updateCosts()
	{
		List<CostWidget> costs = new ArrayList<>();
		this.collectCost(w ->
		{
			costs.add(w);
			return w;
		});
		this.costsList.updateCosts(costs);
	}

	protected void collectCost(ICostConsumer consumer)
	{

	}

	private void updateResults()
	{
		List<AbstractWidget> costs = new ArrayList<>();
		this.collectResult(costs::add);

		this.resultsList.clearChildren();
		costs.forEach(this.resultsList::addChild);
	}

	protected void collectResult(Consumer<AbstractWidget> consumer)
	{

	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int x = this.getX();
		int y = this.getY();
		int costListY = this.configsList.getY();
		int lineTop = y + costListY;
		GuiHelper.fillRectagleBlack(pGuiGraphics, x, lineTop, this.configsList.getWidth(), 1);

		if (!this.costsButton.isSelected())
		{
			GuiHelper.fillRectagleBlack(pGuiGraphics, x + this.costsButton.getX(), lineTop, this.costsButton.getWidth(), 1);
		}

		if (!this.resultsButton.isSelected())
		{
			GuiHelper.fillRectagleBlack(pGuiGraphics, x + this.resultsButton.getX(), lineTop, this.resultsButton.getWidth(), 1);
		}

		GuiHelper.fillRectagleBlack(pGuiGraphics, x, y + costListY, 1, this.height - costListY);
		GuiHelper.fillRectagleBlack(pGuiGraphics, x, y + this.height - 1, this.width, 1);

		super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);

		if (this.needNotifyStateChange)
		{
			this.needNotifyStateChange = false;
			this.updateAll();
			this.notifyStateChange();
		}

	}

	public void initialize()
	{
		if (this.initialzed)
		{
			return;
		}

		this.initialzed = true;
		this.updateAll();
	}

	private void updateAll()
	{
		this.updateResults();
		this.updateCosts();
	}

	public CostWidget getCostUnderMouse(double pMouseX, double pMouseY)
	{
		return this.costsList.getCostUnderMouse(this.toChildX(pMouseX), this.toChildY(pMouseY));
	}

	public void markNeedUpdate()
	{
		this.needNotifyStateChange = true;
	}

	private void notifyStateChange()
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen instanceof IRecipeLogicStateListener listener)
		{
			listener.jei_mekanism_multiblocks$onStateChange();
		}

	}

	public int getDimensionVolume()
	{
		Vec3i dimension = this.getDimension();
		return dimension.getX() * dimension.getY() * dimension.getZ();
	}

	public int getDimensionCornerBlocks()
	{
		Vec3i innerDimension = this.getDimensionInner();
		return 8 + (innerDimension.getX() * 4) + (innerDimension.getZ() * 4) + (innerDimension.getY() * 4);
	}

	public int getDimensionSideBlocks()
	{
		Vec3i innerDimension = this.getDimensionInner();
		return (innerDimension.getX() * innerDimension.getZ() * 2) + (innerDimension.getX() * innerDimension.getY() * 2) + (innerDimension.getZ() * innerDimension.getY() * 2);
	}

	public int getDimensionCasingBlocks()
	{
		return this.getDimensionCornerBlocks() + this.getDimensionSideBlocks();
	}

	public int getCornerBlocks()
	{
		return this.getDimensionCornerBlocks();
	}

	public int getSideBlocks()
	{
		return this.getDimensionSideBlocks();
	}

	public int getDimensionInnerVolume()
	{
		Vec3i inner = this.getDimensionInner();
		return inner.getX() * inner.getY() * inner.getZ();
	}

	public Vec3i getDimensionInner()
	{
		int innerWidth = this.getDimensionWidth() - 2;
		int innerLength = this.getDimensionLength() - 2;
		int innerHeight = this.getDimensionHeight() - 2;
		return new Vec3i(innerWidth, innerHeight, innerLength);
	}

	public Vec3i getDimension()
	{
		int width = this.getDimensionWidth();
		int length = this.getDimensionLength();
		int height = this.getDimensionHeight();
		return new Vec3i(width, height, length);
	}

	public int getDimensionWidth()
	{
		return this.widthWidget.getSlider().getValue();
	}

	public void setDimensionWidth(int width)
	{
		this.widthWidget.getSlider().setValue(width);
	}

	public abstract int getDimensionWidthMin();

	public abstract int getDimensionWidthMax();

	public int getDimensionLength()
	{
		return this.lengthWidget.getSlider().getValue();
	}

	public void setDimensionLength(int length)
	{
		this.lengthWidget.getSlider().setValue(length);
	}

	public abstract int getDimensionLengthMin();

	public abstract int getDimensionLengthMax();

	public int getDimensionHeight()
	{
		return this.heightWidget.getSlider().getValue();
	}

	public void seDimensionHeight(int height)
	{
		this.heightWidget.getSlider().setValue(height);
	}

	public abstract int getDimensionHeightMin();

	public abstract int getDimensionHeightMax();

}
