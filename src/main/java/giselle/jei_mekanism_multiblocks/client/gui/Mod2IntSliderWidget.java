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
	public int getIntValue()
	{
		int intValue = super.getIntValue();

		if (intValue % 2 == this.getRemainder())
		{
			intValue++;
		}

		return intValue;
	}

	@Override
	public void setIntValue(int intValue)
	{
		if (intValue % 2 == this.getRemainder())
		{
			int prev = this.getIntValue();

			if (intValue > prev)
			{
				intValue++;
			}
			else
			{
				intValue--;
			}

		}

		super.setIntValue(intValue);
	}

	public int getRemainder()
	{
		return this.remainder;
	}

}
