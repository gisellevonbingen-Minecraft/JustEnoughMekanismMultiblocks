package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget.Alignment;
import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import mezz.jei.gui.recipes.IRecipeLogicStateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class MultiblockWidget extends ContainerWidget
{
	private final ListWidget configList;
	private final CostList costList;

	private IntSliderWithButtons widthWidget;
	private IntSliderWithButtons lengthWidget;
	private IntSliderWithButtons heightWidget;

	private boolean needNotifyStateChange;

	public MultiblockWidget()
	{
		super(0, 0, 0, 0);

		this.addChild(new LabelWidget(00, 00, 100, 10, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs"), Alignment.Left));
		this.addChild(this.configList = new ListWidget(00, 10, 100, 110, 10));
		this.configList.setItemsPadding(2);
		this.configList.setItemOffset(2);

		this.addChild(new LabelWidget(100, 0, 80, 10, new TranslationTextComponent("text.jei_mekanism_multiblocks.costs"), Alignment.Left));
		this.addChild(this.costList = new CostList(100, 10, 80, 110, 20));

		this.createSpecDimension();

		LabelWidget othersLabel = new LabelWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.others"), Alignment.Left);
		List<Widget> otherWidgets = new ArrayList<>();
		this.collectOtherConfigs(otherWidgets::add);

		if (otherWidgets.size() > 0)
		{
			this.configList.addChild(othersLabel);
			otherWidgets.forEach(this.configList::addChild);
		}

		this.needNotifyStateChange = true;
	}

	protected void collectOtherConfigs(Consumer<Widget> consumer)
	{

	}

	private void createSpecDimension()
	{
		LabelWidget dimensionLabel = new LabelWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.dimensions"), Alignment.Left);

		List<IntSliderWithButtons> widgets = new ArrayList<>();
		widgets.add(this.widthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.dimensions.width", this.getDimensionWidthMin(), this.getDimensionWidthMin(), this.getDimensionWidthMax(), this::onWidthChanged));
		widgets.add(this.lengthWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.dimensions.length", this.getDimensionLengthMin(), this.getDimensionLengthMin(), this.getDimensionLengthMax(), this::onLengthChanged));
		widgets.add(this.heightWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.dimensions.height", this.getDimensionHeightMin(), this.getDimensionHeightMin(), this.getDimensionHeightMax(), this::onHeightChanged));

		List<IntSliderWithButtons> list = widgets.stream().filter(w -> w.getSlider().getMinValue() < w.getSlider().getMaxValue()).collect(Collectors.toList());

		if (list.size() > 0)
		{
			this.configList.addChild(dimensionLabel);
			list.forEach(this.configList::addChild);
		}

	}

	protected void onWidthChanged(int width)
	{
		this.onDimensionChanged();
	}

	protected void onLengthChanged(int length)
	{
		this.onDimensionChanged();
	}

	protected void onHeightChanged(int height)
	{
		this.onDimensionChanged();
	}

	protected void onDimensionChanged()
	{
		this.markNeedUpdateCost();
	}

	public List<ItemStack> getCosts()
	{
		return this.costList.getCosts();
	}

	private void updateCost()
	{
		List<ItemStack> costs = new ArrayList<>();
		this.collectCost(costs::add);
		this.costList.updateCosts(costs);
	}

	protected void collectCost(Consumer<ItemStack> consumer)
	{

	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		if (this.needNotifyStateChange)
		{
			this.needNotifyStateChange = false;
			this.updateCost();
			this.notifyStateChange();
		}

		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, this.y + this.configList.y, this.width, 1);
		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, this.y + this.configList.y, 1, this.height - this.configList.y);
		GuiHelper.fillRectagleBlack(pMatrixStack, this.x, this.y + this.height, this.width, 1);
	}

	public Optional<Object> getIngredientUnderMouse(double pMouseX, double pMouseY)
	{
		return this.costList.getIngredientUnderMouse(this.toChildX(pMouseX), this.toChildY(pMouseY));
	}

	protected void markNeedUpdateCost()
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

	public int getDimensionCornerBlocks()
	{
		Vector3i innerDimension = this.getDimensionInner();
		return 8 + (innerDimension.getX() * 4) + (innerDimension.getZ() * 4) + (innerDimension.getY() * 4);
	}

	public int getDimensionInnerBlocks()
	{
		Vector3i innerDimension = this.getDimensionInner();
		return (innerDimension.getX() * innerDimension.getZ() * 2) + (innerDimension.getX() * innerDimension.getY() * 2) + (innerDimension.getZ() * innerDimension.getY() * 2);
	}

	public int getCornerBlocks()
	{
		return this.getDimensionCornerBlocks();
	}

	public int getInnerBlocks()
	{
		return this.getDimensionInnerBlocks();
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
