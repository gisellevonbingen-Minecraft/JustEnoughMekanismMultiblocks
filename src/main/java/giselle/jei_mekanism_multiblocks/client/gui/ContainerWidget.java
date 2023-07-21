package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.StringTextComponent;

public class ContainerWidget extends Widget
{
	private final List<Widget> children;
	private final List<Widget> unmodifiableChildren;
	private final List<Widget> functionWidgets;
	private final List<Widget> unmodifiableFunctionWidgets;

	private Widget focused;

	public ContainerWidget(int pX, int pY, int pWidth, int pHeight)
	{
		super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY);

		this.children = new ArrayList<>();
		this.unmodifiableChildren = Collections.unmodifiableList(this.children);
		this.functionWidgets = new ArrayList<>();
		this.unmodifiableFunctionWidgets = Collections.unmodifiableList(this.functionWidgets);
	}

	public List<Widget> getChildren()
	{
		return this.unmodifiableChildren;
	}

	public <WIDGET extends Widget> WIDGET addChild(WIDGET widget)
	{
		this.children.add(widget);
		this.onChildAdded(widget);
		return widget;
	}

	public boolean removeChild(Widget widget)
	{
		if (this.children.remove(widget))
		{
			this.onChildRemoved(widget);
			return true;
		}
		else
		{
			return false;
		}

	}

	protected void onChildAdded(Widget widget)
	{

	}

	protected void onChildRemoved(Widget widget)
	{
		if (this.getFocused() == widget)
		{
			this.focused = null;
		}

	}

	public void clearChildren()
	{
		new ArrayList<>(this.getChildren()).forEach(this::removeChild);
	}

	public List<Widget> getFunctionWidgets()
	{
		return this.unmodifiableFunctionWidgets;
	}

	public <WIDGET extends Widget> WIDGET addFunctionWidget(WIDGET widget)
	{
		this.functionWidgets.add(widget);
		this.onFunctionWidgetAdded(widget);
		return widget;
	}

	public boolean removeFunctionWidget(Widget widget)
	{
		if (this.functionWidgets.remove(widget))
		{
			this.onFunctionWidgetRemoved(widget);
			return true;
		}
		else
		{
			return false;
		}

	}

	protected void onFunctionWidgetAdded(Widget widget)
	{

	}

	protected void onFunctionWidgetRemoved(Widget widget)
	{
		if (this.getFocused() == widget)
		{
			this.focused = null;
		}

	}

	public void clearFunctionWidgets()
	{
		new ArrayList<>(this.getFunctionWidgets()).forEach(this::removeFunctionWidget);
	}

	public List<List<Widget>> getFunctionableWidgets()
	{
		return Arrays.asList(this.getChildren(), this.getFunctionWidgets());
	}

	public Widget getFocused()
	{
		return this.focused;
	}

	public Rectangle2d getBounds()
	{
		return new Rectangle2d(this.x, this.y, this.getWidth(), this.getHeight());
	}

	@Override
	public void setWidth(int value)
	{
		int prev = this.getWidth();
		super.setWidth(value);
		int next = this.getWidth();

		if (prev != next)
		{
			this.onWidthChanged();
		}

	}

	protected void onWidthChanged()
	{
		this.onSizeChanged();
	}

	@Override
	public void setHeight(int value)
	{
		int prev = this.getHeight();
		super.setHeight(value);
		int next = this.getHeight();

		if (prev != next)
		{
			this.onHeightChanged();
		}

	}

	protected void onHeightChanged()
	{
		this.onSizeChanged();

	}

	protected void onSizeChanged()
	{

	}

	protected void transformClient(MatrixStack matrixStack)
	{
		matrixStack.translate(this.x, this.y, 0.0D);
	}

	protected double toChildX(double x)
	{
		return x - this.x;
	}

	protected double toChildY(double y)
	{
		return y - this.y;
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		if (this.visible)
		{
			pMatrixStack.pushPose();
			this.transformClient(pMatrixStack);
			int childMouseX = (int) this.toChildX(pMouseX);
			int childMouseY = (int) this.toChildY(pMouseY);

			for (List<Widget> widgets : this.getFunctionableWidgets())
			{
				for (Widget widget : widgets)
				{
					this.onRenderWidget(widgets, widget, pMatrixStack, childMouseX, childMouseY, pPartialTicks);
				}

			}

			pMatrixStack.popPose();
		}

	}

	protected void onRenderWidget(List<Widget> widgets, Widget widget, MatrixStack pMatrixStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pMatrixStack, childMouseX, childMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (List<Widget> widgets : this.getFunctionableWidgets())
			{
				for (Widget widget : widgets)
				{
					if (widget.mouseClicked(childMouseX, childMouseY, pButton))
					{
						this.focused = widget;
						return true;
					}

				}

			}

		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (List<Widget> widgets : this.getFunctionableWidgets())
			{
				for (Widget widget : widgets)
				{
					if (widget.mouseReleased(childMouseX, childMouseY, pButton))
					{
						return true;
					}

				}

			}

		}

		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
	{
		Widget focused = this.getFocused();

		if (focused != null && this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);
			return focused.mouseDragged(childMouseX, childMouseY, pButton, pDragX, pDragY);
		}

		return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (List<Widget> widgets : this.getFunctionableWidgets())
			{
				for (Widget widget : widgets)
				{
					if (widget.mouseScrolled(childMouseX, childMouseY, pDelta))
					{
						return true;
					}

				}

			}

		}

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		super.renderToolTip(pMatrixStack, pMouseX, pMouseY);

		pMatrixStack.pushPose();
		this.transformClient(pMatrixStack);
		int childMouseX = (int) this.toChildX(pMouseX);
		int childMouseY = (int) this.toChildY(pMouseY);

		for (List<Widget> widgets : this.getFunctionableWidgets())
		{
			for (Widget widget : widgets)
			{
				widget.renderToolTip(pMatrixStack, childMouseX, childMouseY);
			}

		}

		pMatrixStack.popPose();
	}

}
