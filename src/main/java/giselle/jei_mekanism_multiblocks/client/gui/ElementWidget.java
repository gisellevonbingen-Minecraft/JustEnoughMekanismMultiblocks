package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import giselle.jei_mekanism_multiblocks.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

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
		this(pX, pY, pWidth, pHeight, TextComponent.EMPTY);
	}

	public ElementWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
	{
		super(pX, pY, pWidth, pHeight, pMessage);

		this.children = new ArrayList<>();
		this.unmodifiableChildren = Collections.unmodifiableList(this.children);

		this.tooltipMessage = Collections.emptyList();
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

			for (AbstractWidget widget : this.getChildren())
			{
				this.onRenderWidgetBackground(widget, pPoseStack, childMouseX, childMouseY, pPartialTicks);
			}

			for (AbstractWidget widget : this.getChildren())
			{
				this.onRenderWidgetForeground(widget, pPoseStack, childMouseX, childMouseY, pPartialTicks);
			}

			pPoseStack.popPose();
		}

	}

	protected void onRenderWidgetBackground(AbstractWidget widget, PoseStack pPoseStack, int childMouseX, int childMouseY, float pPartialTicks)
	{

	}

	protected void onRenderWidgetForeground(AbstractWidget widget, PoseStack pPoseStack, int childMouseX, int childMouseY, float pPartialTicks)
	{
		widget.render(pPoseStack, childMouseX, childMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{

	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
	{
		if (this.visible && this.isHoveredOrFocused())
		{
			GuiHelper.renderComponentTooltip(pPoseStack, pMouseX, pMouseY, this.getTooltipMessage());

			for (AbstractWidget widget : this.getChildren())
			{
				widget.renderToolTip(pPoseStack, pMouseX, pMouseY);
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
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
	{
		if (this.active && this.visible)
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			for (AbstractWidget widget : this.getChildren())
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

	public void onParentChanged(ElementWidget parent)
	{
		this.parent = parent;
	}

	public ElementWidget getParent()
	{
		return this.parent;
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

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput)
	{

	}

}
