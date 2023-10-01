package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ContainerWidget extends AbstractWidget
{
	private final List<AbstractWidget> children;
	private final List<AbstractWidget> unmodifiableChildren;
	private final List<AbstractWidget> functionWidgets;
	private final List<AbstractWidget> unmodifiableFunctionWidgets;

	private AbstractWidget focused;

	public ContainerWidget(int pX, int pY, int pWidth, int pHeight)
	{
		super(pX, pY, pWidth, pHeight, Component.empty());

		this.children = new ArrayList<>();
		this.unmodifiableChildren = Collections.unmodifiableList(this.children);
		this.functionWidgets = new ArrayList<>();
		this.unmodifiableFunctionWidgets = Collections.unmodifiableList(this.functionWidgets);
	}

	public List<AbstractWidget> getChildren()
	{
		return this.unmodifiableChildren;
	}

	public <WIDGET extends AbstractWidget> WIDGET addChild(WIDGET widget)
	{
		this.children.add(widget);
		this.onChildAdded(widget);
		return widget;
	}

	public boolean removeChild(AbstractWidget widget)
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

	protected void onChildAdded(AbstractWidget widget)
	{

	}

	protected void onChildRemoved(AbstractWidget widget)
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

	public List<AbstractWidget> getFunctionWidgets()
	{
		return this.unmodifiableFunctionWidgets;
	}

	public <WIDGET extends AbstractWidget> WIDGET addFunctionWidget(WIDGET widget)
	{
		this.functionWidgets.add(widget);
		this.onFunctionWidgetAdded(widget);
		return widget;
	}

	public boolean removeFunctionWidget(AbstractWidget widget)
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

	protected void onFunctionWidgetAdded(AbstractWidget widget)
	{

	}

	protected void onFunctionWidgetRemoved(AbstractWidget widget)
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

	public List<List<AbstractWidget>> getFunctionableWidgets()
	{
		return Arrays.asList(this.getChildren(), this.getFunctionWidgets());
	}

	public AbstractWidget getFocused()
	{
		return this.focused;
	}

	public Rect2i getBounds()
	{
		return new Rect2i(this.getX(), this.getY(), this.getWidth(), this.getHeight());
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

	protected void transformClient(PoseStack pose)
	{
		pose.translate(this.getX(), this.getY(), 0.0D);
	}

	protected double toChildX(double x)
	{
		return x - this.getX();
	}

	protected double toChildY(double y)
	{
		return y - this.getY();
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);

		if (this.visible)
		{
			PoseStack pose = pGuiGraphics.pose();
			pose.pushPose();
			this.transformClient(pose);
			int childMouseX = (int) this.toChildX(pMouseX);
			int childMouseY = (int) this.toChildY(pMouseY);

			for (List<AbstractWidget> widgets : this.getFunctionableWidgets())
			{
				for (AbstractWidget widget : widgets)
				{
					this.onRenderWidget(widgets, widget, pGuiGraphics, childMouseX, childMouseY, pPartialTicks);
				}

			}

			pose.popPose();
		}

	}

	protected void onRenderWidget(List<AbstractWidget> widgets, AbstractWidget widget, GuiGraphics pGuiGraphics, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pGuiGraphics, childMouseX, childMouseY, pPartialTicks);
	}

	@Override
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (List<AbstractWidget> widgets : this.getFunctionableWidgets())
			{
				for (AbstractWidget widget : widgets)
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
		AbstractWidget focused = this.getFocused();
		this.focused = null;

		if (focused != null && this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);
			return focused.mouseReleased(childMouseX, childMouseY, pButton);
		}

		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
	{
		AbstractWidget focused = this.getFocused();

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

			for (List<AbstractWidget> widgets : this.getFunctionableWidgets())
			{
				for (AbstractWidget widget : widgets)
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
	public void playDownSound(SoundManager pHandler)
	{

	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
