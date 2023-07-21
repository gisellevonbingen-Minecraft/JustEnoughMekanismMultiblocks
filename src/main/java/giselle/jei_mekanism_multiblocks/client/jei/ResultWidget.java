package giselle.jei_mekanism_multiblocks.client.jei;

import giselle.jei_mekanism_multiblocks.client.gui.ContainerWidget;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.TextAlignment;
import net.minecraft.util.text.ITextComponent;

public class ResultWidget extends ContainerWidget
{
	private final LabelWidget textLabel;
	private final LabelWidget valueLabel;

	public ResultWidget(int pX, int pY, int pWidth, int pHeight, ITextComponent text, ITextComponent value)
	{
		super(pX, pY, pWidth, pHeight);

		this.addChild(this.textLabel = new LabelWidget(0, 0, 0, 0, text, TextAlignment.LEFT));
		this.addChild(this.valueLabel = new LabelWidget(0, 0, 0, 0, value, TextAlignment.RIGHT));

		this.updateChildrenHorizontal();
		this.updateChildrenVertical();
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
		textLabel.y = 0;
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

}
