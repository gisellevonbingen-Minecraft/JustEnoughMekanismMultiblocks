package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;

public class ContainerWidget extends AbstractWidget
{
	private final List<AbstractWidget> children;
	private final List<AbstractWidget> unmodifiableChildren;
	private final List<AbstractWidget> functionWidgets;
	private final List<AbstractWidget> unmodifiableFunctionWidgets;

	private AbstractWidget focused;

	public ContainerWidget(int pX, int pY, int pWidth, int pHeight)
	{
		super(pX, pY, pWidth, pHeight, TextComponent.EMPTY);

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
		return new Rect2i(this.x, this.y, this.getWidth(), this.getHeight());
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

	protected void transformClient(PoseStack pPoseStack)
	{
		pPoseStack.translate(this.x, this.y, 0.0D);
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
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTicks);

		if (this.visible)
		{
			pPoseStack.pushPose();
			this.transformClient(pPoseStack);
			int childMouseX = (int) this.toChildX(pMouseX);
			int childMouseY = (int) this.toChildY(pMouseY);

			for (List<AbstractWidget> widgets : this.getFunctionableWidgets())
			{
				for (AbstractWidget widget : widgets)
				{
					this.onRenderWidget(widgets, widget, pPoseStack, childMouseX, childMouseY, pPartialTicks);
				}

			}

			pPoseStack.popPose();
		}

	}

	protected void onRenderWidget(List<AbstractWidget> widgets, AbstractWidget widget, PoseStack pPoseStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pPoseStack, childMouseX, childMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
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
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
	{
		super.renderToolTip(pPoseStack, pMouseX, pMouseY);

		pPoseStack.pushPose();
		this.transformClient(pPoseStack);
		int childMouseX = (int) this.toChildX(pMouseX);
		int childMouseY = (int) this.toChildY(pMouseY);

		for (List<AbstractWidget> widgets : this.getFunctionableWidgets())
		{
			for (AbstractWidget widget : widgets)
			{
				widget.renderToolTip(pPoseStack, childMouseX, childMouseY);
			}

		}

		pPoseStack.popPose();
	}

	@Override
	public void playDownSound(SoundManager pHandler)
	{

	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
