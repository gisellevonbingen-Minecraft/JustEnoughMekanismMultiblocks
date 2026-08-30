package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;

import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;

public class ResultWidget extends ContainerWidget
{
	private final LabelWidget textLabel;
	private final LabelWidget valueLabel;

	private final List<IPressHandler> pressHandlers;

	public ResultWidget(Component text, Component value)
	{
		this(0, 0, 0, 0, text, value);
	}

	public ResultWidget(int pX, int pY, int pWidth, int pHeight, Component text, Component value)
	{
		super(pX, pY, pWidth, pHeight);

		this.addChild(this.textLabel = new LabelWidget(0, 0, 0, 0, text, TextAlignment.LEFT));
		this.textLabel.setFGColor(0x3F3F3F);
		this.textLabel.setShadow(false);
		this.addChild(this.valueLabel = new LabelWidget(0, 0, 0, 0, value, TextAlignment.RIGHT));
		this.valueLabel.setFGColor(0x3F3F3F);
		this.valueLabel.setShadow(false);

		this.pressHandlers = new ArrayList<>();

		this.updateChildrenHorizontal();
		this.updateChildrenVertical();
	}

	public void addPressHandler(IPressHandler handler)
	{
		this.pressHandlers.add(handler);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (super.mouseClicked(pMouseX, pMouseY, pButton))
		{
			for (IPressHandler handler : this.pressHandlers)
			{
				handler.onPress(this);
			}

			return true;
		}

		return false;
	}

	@Override
	protected void onWidthChanged()
	{
		super.onWidthChanged();

		this.updateChildrenHorizontal();
	}

	@Override
	protected void onHeightChanged()
	{
		super.onHeightChanged();

		this.updateChildrenVertical();
	}

	private void updateChildrenHorizontal()
	{
		int width = this.getWidth();

		LabelWidget textLabel = this.getTextLabel();
		textLabel.x = 0;
		textLabel.setWidth(width);

		LabelWidget valueLabel = this.getValueLabel();
		valueLabel.x = 0;
		valueLabel.setWidth(width);
	}

	private void updateChildrenVertical()
	{
		int height = this.getHeight() / 2;

		LabelWidget textLabel = this.getTextLabel();
		textLabel.y = 1;
		textLabel.setHeight(height);

		LabelWidget valueLabel = this.getValueLabel();
		valueLabel.y = textLabel.y + textLabel.getHeight();
		valueLabel.setHeight(height);
	}

	public LabelWidget getTextLabel()
	{
		return this.textLabel;
	}

	public LabelWidget getValueLabel()
	{
		return this.valueLabel;
	}

	public void setTooltip(Component... tooltip)
	{
		this.getTextLabel().setTooltip(tooltip);
		this.getValueLabel().setTooltip(tooltip);
	}

	public interface IPressHandler
	{
		void onPress(ResultWidget widget);
	}

}
