package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TabButtonWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import mezz.jei.gui.recipes.IRecipeLogicStateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class MultiblockWidget extends ContainerWidget
{
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

		this.addChild(new LabelWidget(00, 00, 100, 10, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs"), TextAlignment.LEFT));
		this.addChild(this.configsList = new ListWidget(00, 10, 100, 110, 10));
		this.configsList.setItemsPadding(2);
		this.configsList.setItemOffset(2);

		this.addChild(this.costsButton = new TabButtonWidget(99, 0, 41, 10, new TranslationTextComponent("text.jei_mekanism_multiblocks.costs")));
		this.costsButton.addPressHandler(this::onCostsButtonClick);
		this.addChild(this.resultsButton = new TabButtonWidget(139, 0, 41, 10, new TranslationTextComponent("text.jei_mekanism_multiblocks.results")));
		this.resultsButton.addPressHandler(this::onResultsButtonClick);
		this.addChild(this.costsList = new CostList(100, 10, 80, 110, 20));
		this.costsList.setItemsTop(2);
		this.costsList.setItemOffset(1);
		this.addChild(this.resultsList = new ListLineWidget(100, 10, 80, 110, 20));
		this.resultsList.setItemsPadding(2);
		this.resultsList.setItemOffset(1);

		this.createSpecDimension();

		LabelWidget othersLabel = new LabelWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.others"), TextAlignment.LEFT);
		List<Widget> otherWidgets = new ArrayList<>();
		this.collectOtherConfigs(otherWidgets::add);

		if (otherWidgets.size() > 0)
		{
			this.configsList.addChild(othersLabel);
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

	protected void collectOtherConfigs(Consumer<Widget> consumer)
	{

	}

	private void createSpecDimension()
	{
		LabelWidget dimensionLabel = new LabelWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.dimensions"), TextAlignment.LEFT);

		List<IntSliderWithButtons> widgets = new ArrayList<>();
		widgets.add(this.widthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.dimensions.width", this.createDimensionSlider(0, this.getDimensionWidthMin(), this.getDimensionWidthMax())));
		this.widthWidget.getSlider().addIntValueChangeHanlder(this::onDimensionWidthChanged);
		widgets.add(this.lengthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.dimensions.length", this.createDimensionSlider(1, this.getDimensionLengthMin(), this.getDimensionLengthMax())));
		this.lengthWidget.getSlider().addIntValueChangeHanlder(this::onDimensionLengthChanged);
		widgets.add(this.heightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.dimensions.height", this.createDimensionSlider(2, this.getDimensionHeightMin(), this.getDimensionHeightMax())));
		this.heightWidget.getSlider().addIntValueChangeHanlder(this::onDimensionHeightChanged);

		List<IntSliderWithButtons> list = widgets.stream().filter(w -> this.isUseDimensionWIidget(w) && w.getSlider().getIntMinValue() < w.getSlider().getIntMaxValue()).collect(Collectors.toList());

		if (list.size() > 0)
		{
			this.configsList.addChild(dimensionLabel);
			list.forEach(this.configsList::addChild);
		}

	}

	protected IntSliderWidget createDimensionSlider(int index, int min, int max)
	{
		return new IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, min, min, max);
	}

	protected boolean isUseDimensionWIidget(IntSliderWithButtons widget)
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
		this.markNeedUpdateCost();
	}

	public List<ItemStack> getCosts()
	{
		return this.costsList.getCosts();
	}

	public void updateCosts()
	{
		List<CostWidget> costs = new ArrayList<>();
		this.collectCost(w ->
		{
			costs.add(w);
			return w;
		});
		this.costsList.updateCosts(costs);
	}

	public void collectCost(ICostConsumer output)
	{

	}

	public void updateResults()
	{
		List<Widget> costs = new ArrayList<>();
		this.collectResult(costs::add);

		this.resultsList.clearChildren();
		costs.forEach(this.resultsList::addChild);
	}

	public void collectResult(Consumer<Widget> consumer)
	{

	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		int lineTop = this.y + this.configsList.y;
		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, lineTop, this.configsList.getWidth(), 1);

		if (!this.costsButton.isSelected())
		{
			GuiHelper.fillRectagleBlack(pMatrixStack, this.x + this.costsButton.x, lineTop, this.costsButton.getWidth(), 1);
		}

		if (!this.resultsButton.isSelected())
		{
			GuiHelper.fillRectagleBlack(pMatrixStack, this.x + this.resultsButton.x, lineTop, this.resultsButton.getWidth(), 1);
		}

		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, this.y + this.configsList.y, 1, this.height - this.configsList.y);
		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, this.y + this.height - 1, this.width, 1);

		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		if (this.needNotifyStateChange)
		{
			this.needNotifyStateChange = false;
			this.updateResults();
			this.updateCosts();
			this.notifyStateChange();
		}

	}

	public Optional<Object> getIngredientUnderMouse(double pMouseX, double pMouseY)
	{
		return this.costsList.getIngredientUnderMouse(this.toChildX(pMouseX), this.toChildY(pMouseY));
	}

	public void markNeedUpdateCost()
	{
		this.needNotifyStateChange = true;
	}

	private void notifyStateChange()
	{
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.screen instanceof IRecipeLogicStateListener)
		{
			((IRecipeLogicStateListener) minecraft.screen).onStateChange();
		}

	}

	public int getDimensionVolume()
	{
		Vector3i dimension = this.getDimension();
		return dimension.getX() * dimension.getY() * dimension.getZ();
	}

	public int getDimensionCornerBlocks()
	{
		Vector3i innerDimension = this.getDimensionInner();
		return 8 + (innerDimension.getX() * 4) + (innerDimension.getZ() * 4) + (innerDimension.getY() * 4);
	}

	public int getDimensionSideBlocks()
	{
		Vector3i innerDimension = this.getDimensionInner();
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

	public Vector3i getDimensionInner()
	{
		int innerWidth = this.getDimensionWidth() - 2;
		int innerLength = this.getDimensionLength() - 2;
		int innerHeight = this.getDimensionHeight() - 2;
		return new Vector3i(innerWidth, innerHeight, innerLength);
	}

	public Vector3i getDimension()
	{
		int width = this.getDimensionWidth();
		int length = this.getDimensionLength();
		int height = this.getDimensionHeight();
		return new Vector3i(width, height, length);
	}

	public int getDimensionWidth()
	{
		return this.widthWidget.getSlider().getIntValue();
	}

	public void setDimensionWidth(int width)
	{
		this.widthWidget.getSlider().setIntValue(width);
	}

	public abstract int getDimensionWidthMin();

	public abstract int getDimensionWidthMax();

	public int getDimensionLength()
	{
		return this.lengthWidget.getSlider().getIntValue();
	}

	public void setDimensionLength(int length)
	{
		this.lengthWidget.getSlider().setIntValue(length);
	}

	public abstract int getDimensionLengthMin();

	public abstract int getDimensionLengthMax();

	public int getDimensionHeight()
	{
		return this.heightWidget.getSlider().getIntValue();
	}

	public void seDimensionHeight(int height)
	{
		this.heightWidget.getSlider().setIntValue(height);
	}

	public abstract int getDimensionHeightMin();

	public abstract int getDimensionHeightMax();

}
