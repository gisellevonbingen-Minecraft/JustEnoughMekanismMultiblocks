package giselle.jei_mekanism_multiblocks.client.gui;

import net.minecraft.util.text.ITextComponent;

public class Mod2IntSliderWidget extends IntSliderWidget
{
	private int remainder;

	public Mod2IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max, int remainder)
	{
		super(x, y, width, height, pMessage, value, min, max);

		this.remainder = remainder;
	}

	@Override
	protected int toValue(double pRatio)
	{
		int value = super.toValue(pRatio);

		if (value % 2 == this.getRemainder())
		{
			value++;
		}

		return value;
	}

	@Override
	public void setValue(int value)
	{
		if (value % 2 == this.getRemainder())
		{
			int prev = this.getValue();

			if (value > prev)
			{
				value++;
			}
			else
			{
				value--;
			}

		}

		super.setValue(value);
	}

	public int getRemainder()
	{
		return this.remainder;
	}

}
