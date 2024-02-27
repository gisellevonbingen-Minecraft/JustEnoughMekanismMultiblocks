package giselle.jei_mekanism_multiblocks.client.jei;

import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;

public class ResultWidget extends ContainerWidget
{
	private final LabelWidget textLabel;
	private final LabelWidget valueLabel;

	private Component[] jeiTooltip;

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

		this.updateChildrenHorizontal();
		this.updateChildrenVertical();

		this.jeiTooltip = new Component[0];
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
		textLabel.setX(0);
		textLabel.setWidth(width);

		LabelWidget valueLabel = this.getValueLabel();
		valueLabel.setX(0);
		valueLabel.setWidth(width);
	}

	private void updateChildrenVertical()
	{
		int height = this.getHeight() / 2;

		LabelWidget textLabel = this.getTextLabel();
		textLabel.setY(1);
		textLabel.setHeight(height);

		LabelWidget valueLabel = this.getValueLabel();
		valueLabel.setY(textLabel.getY() + textLabel.getHeight());
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

	public Component[] getJeiTooltip()
	{
		return jeiTooltip;
	}

	public void setJeiTooltip(Component... jeiTooltip)
	{
		this.jeiTooltip = jeiTooltip.clone();
	}

}
