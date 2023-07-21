package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.IntConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class IntSliderWidget extends SliderWidget
{
	private final IntConsumer setter;
	private int minValue;
	private int maxValue;

	public IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max)
	{
		this(x, y, width, height, pMessage, value, min, max, null);
	}

	public IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max, IntConsumer setter)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
		this.minValue = min;
		this.maxValue = max;
		this.setter = setter;
	}

	@Override
	protected double applyValueFromMouse(double pValue)
	{
		int min = this.getMinValue();
		int max = this.getMaxValue();
		int intValue = (int) Math.round(MathHelper.clampedLerp(min, max, pValue));
		return MathHelper.inverseLerp(intValue, min, max);
	}

	@Override
	protected void onValueChanged()
	{
		super.onValueChanged();

		this.onIntValueChanged();
	}

	private void onIntValueChanged()
	{
		this.setter.accept(this.getIntValue());
	}

	public int getIntValue()
	{
		int min = this.getMinValue();
		int max = this.getMaxValue();

		if (min == max)
		{
			return min;
		}
		else
		{
			return (int) Math.round(MathHelper.clampedLerp(min, max, this.getValue()));
		}

	}

	public void setIntValue(int value)
	{
		value = MathHelper.clamp(value, this.getMinValue(), this.getMaxValue());
		double doubleValue = MathHelper.inverseLerp(value, this.getMinValue(), this.getMaxValue());

		if (this.getIntValue() != value)
		{
			this.setValue(doubleValue);
		}
		else
		{
			this.setValueInternal(doubleValue);
		}

	}

	public int getMinValue()
	{
		return this.minValue;
	}

	public void setMinValue(int minValue)
	{
		if (this.getMinValue() != minValue)
		{
			this.minValue = minValue;
			this.onIntValueChanged();
		}

	}

	public int getMaxValue()
	{
		return this.maxValue;
	}

	public void setMaxValue(int maxValue)
	{
		if (this.getMaxValue() != maxValue)
		{
			this.maxValue = maxValue;
			this.onIntValueChanged();
		}

	}

}
