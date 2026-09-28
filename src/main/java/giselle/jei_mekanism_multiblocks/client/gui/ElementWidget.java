package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ElementWidget extends AbstractWidget
{
	private final List<AbstractWidget> children;
	private final List<AbstractWidget> unmodifiableChildren;

	private ElementWidget parent;
	private List<Component> tooltipMessage;

	private AbstractWidget focused;

	protected boolean playDownSound;

	public ElementWidget(int pX, int pY, int pWidth, int pHeight)
	{
		this(pX, pY, pWidth, pHeight, Component.empty());
	}

	public ElementWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);

		this.children = new ArrayList<>();
		this.unmodifiableChildren = Collections.unmodifiableList(this.children);

		this.tooltipMessage = Collections.emptyList();
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer)
	{
		super.visitWidgets(consumer);
		this.getChildren().forEach(consumer);
	}

	public boolean contains(AbstractWidget widget)
	{
		return this.children.contains(widget);
	}

	public AbstractWidget getChildUnderMouse(double pMouseX, double pMouseY)
	{
		double childMouseX = this.toChildX(pMouseX);
		double childMouseY = this.toChildY(pMouseY);

		for (AbstractWidget widget : this.getChildren())
		{
			if (widget.isMouseOver(childMouseX, childMouseY))
			{
				return widget;
			}

		}

		return null;
	}

	public List<Component> getTooltip(double pMouseX, double pMouseY)
	{
		if (this.isMouseOver(pMouseX, pMouseY))
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (AbstractWidget widget : this.getChildren())
			{
				if (widget.isMouseOver(childMouseX, childMouseY))
				{
					if (widget instanceof ElementWidget elementWidget)
					{
						List<Component> tooltip = elementWidget.getTooltip(childMouseX, childMouseY);

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
		if (widget instanceof ElementWidget elementWidget)
		{
			elementWidget.onParentChanged(this);
		}

	}

	protected void onChildRemoved(AbstractWidget widget)
	{
		if (widget instanceof ElementWidget elementWidget)
		{
			elementWidget.onParentChanged(null);
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
	public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
	{
		PoseStack pose = pGuiGraphics.pose();
		pose.pushPose();
		this.transformClient(pose);
		int childMouseX = (int) this.toChildX(pMouseX);
		int childMouseY = (int) this.toChildY(pMouseY);

		for (AbstractWidget widget : this.getChildren())
		{
			this.onRenderWidgetBackground(widget, pGuiGraphics, childMouseX, childMouseY, pPartialTicks);
		}

		for (AbstractWidget widget : this.getChildren())
		{
			this.onRenderWidgetForeground(widget, pGuiGraphics, childMouseX, childMouseY, pPartialTicks);
		}

		pose.popPose();
	}

	protected void onRenderWidgetBackground(AbstractWidget widget, GuiGraphics pGuiGraphics, int childMouseX, int childMouseY, float pPartialTicks)
	{

	}

	protected void onRenderWidgetForeground(AbstractWidget widget, GuiGraphics pGuiGraphics, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pGuiGraphics, childMouseX, childMouseY, pPartialTicks);
	}

	public void renderToolTip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHoveredOrFocused())
		{
			GuiHelper.renderComponentTooltip(pGuiGraphics, pMouseX, pMouseY, this.getTooltipMessage());

			for (AbstractWidget widget : this.getChildren())
			{
				if (widget instanceof ElementWidget elementWidget)
				{
					elementWidget.renderToolTip(pGuiGraphics, pMouseX, pMouseY);
				}

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

			for (AbstractWidget widget : this.getChildren())
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
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (AbstractWidget widget : this.getChildren())
			{
				if (widget.mouseScrolled(childMouseX, childMouseY, pScrollX, pScrollY))
				{
					return true;
				}

			}

		}

		return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
	}

	@Override
	public void playDownSound(SoundManager pHandler)
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

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

	public void setTooltipMessage(Component... tooltip)
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

	public void setTooltipMessage(List<Component> tooltip)
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

	public List<Component> getTooltipMessage()
	{
		return new ArrayList<>(this.tooltipMessage);
	}

	public void onParentChanged(ElementWidget parent)
	{
		this.parent = parent;
	}

	public ElementWidget getParent()
	{
		return this.parent;
	}

}
