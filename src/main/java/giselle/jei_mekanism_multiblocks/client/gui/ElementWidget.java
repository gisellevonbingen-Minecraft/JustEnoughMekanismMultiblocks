package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ElementWidget extends Widget
{
	private final List<Widget> children;
	private final List<Widget> unmodifiableChildren;

	private ElementWidget parent;
	private List<ITextComponent> tooltipMessage;

	private Widget focused;

	protected boolean playDownSound;

	public ElementWidget(int pX, int pY, int pWidth, int pHeight)
	{
		this(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY);
	}

	public ElementWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);

		this.children = new ArrayList<>();
		this.unmodifiableChildren = Collections.unmodifiableList(this.children);

		this.tooltipMessage = Collections.emptyList();
	}

	public boolean contains(Widget widget)
	{
		return this.children.contains(widget);
	}

	public Widget getChildUnderMouse(double pMouseX, double pMouseY)
	{
		double childMouseX = this.toChildX(pMouseX);
		double childMouseY = this.toChildY(pMouseY);

		for (Widget widget : this.getChildren())
		{
			if (widget.isMouseOver(childMouseX, childMouseY))
			{
				return widget;
			}

		}

		return null;
	}

	public List<ITextComponent> getTooltip(double pMouseX, double pMouseY)
	{
		if (this.isMouseOver(pMouseX, pMouseY))
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (Widget widget : this.getChildren())
			{
				if (widget.isMouseOver(childMouseX, childMouseY))
				{
					if (widget instanceof ElementWidget)
					{
						List<ITextComponent> tooltip = ((ElementWidget) widget).getTooltip(childMouseX, childMouseY);

						if (tooltip.size() > 0)
						{
							return tooltip;
						}

					}

				}

			}

			return this.getTooltipMessage();
		}

		return Collections.emptyList();
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
		if (widget instanceof ElementWidget)
		{
			((ElementWidget) widget).onParentChanged(this);
		}

	}

	protected void onChildRemoved(Widget widget)
	{
		if (widget instanceof ElementWidget)
		{
			((ElementWidget) widget).onParentChanged(null);
		}

		if (this.getFocused() == widget)
		{
			this.focused = null;
		}

	}

	public void clearChildren()
	{
		new ArrayList<>(this.getChildren()).forEach(this::removeChild);
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

			for (Widget widget : this.getChildren())
			{
				this.onRenderWidgetBackground(widget, pMatrixStack, childMouseX, childMouseY, pPartialTicks);
			}

			for (Widget widget : this.getChildren())
			{
				this.onRenderWidgetForeground(widget, pMatrixStack, childMouseX, childMouseY, pPartialTicks);
			}

			pMatrixStack.popPose();
		}

	}

	protected void onRenderWidgetBackground(Widget widget, MatrixStack pMatrixStack, int childMouseX, int childMouseY, float pPartialTicks)
	{

	}

	protected void onRenderWidgetForeground(Widget widget, MatrixStack pMatrixStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pMatrixStack, childMouseX, childMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	@Override
	public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHovered())
		{
			GuiHelper.renderComponentTooltip(pMatrixStack, pMouseX, pMouseY, this.getTooltipMessage());

			for (Widget widget : this.getChildren())
			{
				widget.renderToolTip(pMatrixStack, pMouseX, pMouseY);
			}

		}

	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (Widget widget : this.getChildren())
			{
				if (widget.mouseClicked(childMouseX, childMouseY, pButton))
				{
					this.focused = widget;
					return true;
				}

			}

		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton)
	{
		Widget focused = this.getFocused();
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

			for (Widget widget : this.getChildren())
			{
				if (widget.mouseScrolled(childMouseX, childMouseY, pDelta))
				{
					return true;
				}

			}

		}

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public void playDownSound(SoundHandler pHandler)
	{
		if (this.playDownSound)
		{
			super.playDownSound(pHandler);
		}

	}

	protected void playDownSound()
	{
		super.playDownSound(Minecraft.getInstance().getSoundManager());
	}

	public void onParentChanged(ElementWidget parent)
	{
		this.parent = parent;
	}

	public ElementWidget getParent()
	{
		return this.parent;
	}

	public void setTooltipMessage(ITextComponent... tooltip)
	{
		if (tooltip == null || tooltip.length == 0)
		{
			this.tooltipMessage = Collections.emptyList();
		}
		else
		{
			this.tooltipMessage = ImmutableList.copyOf(tooltip);
		}

		this.onTooltipMessageChanged();
	}

	public void setTooltipMessage(List<ITextComponent> tooltip)
	{
		if (tooltip == null || tooltip.size() == 0)
		{
			this.tooltipMessage = Collections.emptyList();
		}
		else
		{
			this.tooltipMessage = ImmutableList.copyOf(tooltip);
		}

		this.onTooltipMessageChanged();
	}

	protected void onTooltipMessageChanged()
	{

	}

	public List<ITextComponent> getTooltipMessage()
	{
		return new ArrayList<>(this.tooltipMessage);
	}

}
