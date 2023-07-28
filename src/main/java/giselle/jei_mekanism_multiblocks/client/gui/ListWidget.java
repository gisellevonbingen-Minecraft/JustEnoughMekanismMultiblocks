package giselle.jei_mekanism_multiblocks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.StringTextComponent;

public class ListWidget extends ContainerWidget
{
	private int itemsLeft;
	private int itemsTop;
	private int itemsRight;
	private int itemsBottom;
	private int itemHeight;
	private int itemOffset;
	private boolean itemsHorizontalChanged;
	private boolean itemsVerticalChanged;
	private boolean itemsChanged;

	private int scrollBarWidth;
	private boolean scrollBarHorizontalChanged;
	private boolean scrollBarVerticalChanged;

	private ButtonWidget upButton;
	private ButtonWidget downButton;
	private IntSliderWidget scrollBar;

	public ListWidget(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight);

		this.itemsLeft = 0;
		this.itemsTop = 0;
		this.itemsRight = 0;
		this.itemsBottom = 0;
		this.itemHeight = itemHeight;
		this.itemOffset = 0;
		this.scrollBarWidth = 12;

		this.addFunctionWidget(this.upButton = new ButtonWidget(0, 0, 0, 0, new StringTextComponent("▲"), this::onScrollButtonClick));
		this.addFunctionWidget(this.downButton = new ButtonWidget(0, 0, 0, 0, new StringTextComponent("▼"), this::onScrollButtonClick));
		this.addFunctionWidget(this.scrollBar = new IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, 0, 0, 0, this::onScrollChanged));

		this.scrollBar.setVertical();
		this.itemsChanged = true;
		this.updateScrollWidgetsHorizontal();
		this.updateScrollWidgetsVertical();
	}

	protected void onScrollChanged(int scroll)
	{
		this.itemsVerticalChanged = true;
	}

	protected void onScrollButtonClick(AbstractButton button)
	{
		if (button == this.upButton)
		{
			this.setScrollAmount(this.getScrollAmount() - 1);
		}
		else if (button == this.downButton)
		{
			this.setScrollAmount(this.getScrollAmount() + 1);
		}

	}

	public int getItemCountInHeight()
	{
		int height = this.getHeight();
		int itemHeight = this.getItemHeight();
		int itemOffset = this.getItemOffset();
		return (height + itemOffset) / (itemHeight + itemOffset);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
	{
		if (this.active && this.visible)
		{
			if (this.isMouseOver(pMouseX, pMouseY))
			{
				long scrollDelta = Math.round(pDelta / Math.abs(pDelta));
				this.setScrollAmount(this.getScrollAmount() - (int) scrollDelta);
			}

		}

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		if (this.scrollBarHorizontalChanged)
		{
			this.scrollBarHorizontalChanged = false;
			this.updateScrollWidgetsHorizontal();
		}

		if (this.scrollBarVerticalChanged)
		{
			this.scrollBarVerticalChanged = false;
			this.updateScrollWidgetsVertical();
		}

		if (this.itemsChanged)
		{
			this.itemsChanged = false;

			int scroll = this.scrollBar.getIntValue();
			int childCount = this.getChildren().size();
			this.scrollBar.setIntMaxValue(Math.max(childCount - this.getItemCountInHeight(), 0));
			this.scrollBar.setIntValue(scroll);
			this.scrollBar.active = this.scrollBar.getIntMaxValue() > 0;
			this.upButton.active = this.scrollBar.active;
			this.downButton.active = this.scrollBar.active;
		}

		if (this.itemsHorizontalChanged)
		{
			this.itemsHorizontalChanged = false;
			this.updateItemsHorizontal();
		}

		if (this.itemsVerticalChanged)
		{
			this.itemsVerticalChanged = false;
			this.updateItemsVertical();
		}

		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	protected void updateItemsVertical()
	{
		int itemHeight = this.getItemHeight();
		int itemOffset = this.getItemOffset();
		int itemY = this.getItemsTop() + -this.getScrollAmount() * (itemHeight + itemOffset);
		int top = 0;
		int bottom = this.getHeight() - this.getItemsBottom() - itemHeight;

		for (Widget widget : this.getChildren())
		{
			widget.y = itemY;
			widget.setHeight(itemHeight);
			widget.visible = top <= itemY && itemY <= bottom;

			itemY += itemHeight + itemOffset;
		}

	}

	protected void updateItemsHorizontal()
	{
		for (Widget widget : this.getChildren())
		{
			this.updateItemHorizontal(widget);
		}

	}

	protected void updateItemHorizontal(Widget widget)
	{
		widget.x = this.getItemsLeft();
		widget.setWidth(this.scrollBar.x - this.getItemsRight() - widget.x);
	}

	@Override
	protected void onChildAdded(Widget widget)
	{
		super.onChildAdded(widget);

		this.updateItemHorizontal(widget);
		this.itemsChanged = true;
		this.itemsVerticalChanged = true;
	}

	@Override
	protected void onChildRemoved(Widget widget)
	{
		super.onChildRemoved(widget);

		this.itemsChanged = true;
		this.itemsVerticalChanged = true;
	}

	private void updateScrollWidgetsHorizontal()
	{
		int scrollBarWidth = this.getScrollBarWidth();
		int x = this.getWidth() - scrollBarWidth;

		this.upButton.setWidth(scrollBarWidth);
		this.upButton.x = x;

		this.downButton.setWidth(scrollBarWidth);
		this.downButton.x = this.upButton.x;

		this.scrollBar.setWidth(scrollBarWidth);
		this.scrollBar.x = this.downButton.x;

		this.itemsHorizontalChanged = true;
	}

	private void updateScrollWidgetsVertical()
	{
		int scrollBarWidth = this.getScrollBarWidth();
		this.upButton.setHeight(scrollBarWidth);
		this.upButton.y = 0;

		this.downButton.setHeight(scrollBarWidth);
		this.downButton.y = this.getHeight() - this.downButton.getHeight();

		this.scrollBar.y = this.upButton.y + this.upButton.getHeight();
		this.scrollBar.setHeight(this.downButton.y - this.scrollBar.y);

		this.itemsVerticalChanged = true;
	}

	@Override
	protected void onWidthChanged()
	{
		super.onWidthChanged();
		this.scrollBarHorizontalChanged = true;
	}

	@Override
	protected void onHeightChanged()
	{
		super.onHeightChanged();
		this.scrollBarVerticalChanged = true;
	}

	public int getItemsLeft()
	{
		return this.itemsLeft;
	}

	public void setItemsLeft(int itemsLeft)
	{
		itemsLeft = Math.max(itemsLeft, 0);

		if (this.getItemsLeft() != itemsLeft)
		{
			this.itemsLeft = itemsLeft;
			this.itemsHorizontalChanged = true;
		}

	}

	public int getItemsRight()
	{
		return this.itemsRight;
	}

	public void setItemsRight(int itemsRight)
	{
		itemsRight = Math.max(itemsRight, 0);

		if (this.getItemsRight() != itemsRight)
		{
			this.itemsRight = itemsRight;
			this.itemsHorizontalChanged = true;
		}

	}

	public int getItemsTop()
	{
		return this.itemsTop;
	}

	public void setItemsTop(int itemsTop)
	{
		itemsTop = Math.max(itemsTop, 0);

		if (this.getItemsTop() != itemsTop)
		{
			this.itemsTop = itemsTop;
			this.itemsVerticalChanged = true;
		}

	}

	public int getItemsBottom()
	{
		return this.itemsBottom;
	}

	public void setItemsBottom(int itemsBottom)
	{
		itemsBottom = Math.max(itemsBottom, 0);

		if (this.getItemsBottom() != itemsBottom)
		{
			this.itemsBottom = itemsBottom;
			this.itemsVerticalChanged = true;
		}

	}

	public void setItemsPadding(int padding)
	{
		this.setItemsLeft(padding);
		this.setItemsTop(padding);
		this.setItemsRight(padding);
		this.setItemsBottom(padding);
	}

	public int getItemHeight()
	{
		return this.itemHeight;
	}

	public void setItemHeight(int itemHeight)
	{
		itemHeight = Math.max(itemHeight, 0);
	}

	public int getItemOffset()
	{
		return this.itemOffset;
	}

	public void setItemOffset(int itemOffset)
	{
		itemOffset = Math.max(itemOffset, 0);

		if (this.getItemOffset() != itemOffset)
		{
			this.itemOffset = Math.max(itemOffset, 0);
			this.itemsVerticalChanged = true;
		}

	}

	public int getScrollBarWidth()
	{
		return this.scrollBarWidth;
	}

	public void setScrollBarWidth(int scrollBarWidth)
	{
		scrollBarWidth = Math.max(scrollBarWidth, 0);

		if (this.getScrollBarWidth() != scrollBarWidth)
		{
			this.scrollBarWidth = scrollBarWidth;
			this.scrollBarHorizontalChanged = true;
		}

	}

	public int getScrollMin()
	{
		return 0;
	}

	public int getScrollAmount()
	{
		return this.scrollBar.getIntValue();
	}

	public void setScrollAmount(int scrollAmount)
	{
		this.scrollBar.setIntValue(scrollAmount);
	}

}
