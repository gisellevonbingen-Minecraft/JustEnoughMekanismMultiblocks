package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.IntConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class IntSliderWidget extends SliderWidget
{
	private final IntConsumer setter;
	private int intMinValue;
	private int intMaxValue;

	public IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max)
	{
		this(x, y, width, height, pMessage, value, min, max, null);
	}

	public IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max, IntConsumer setter)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
		this.intMinValue = min;
		this.intMaxValue = max;
		this.setter = setter;
	}

	@Override
	protected double applyValueFromMouse(double pValue)
	{
		int intMin = this.getIntMinValue();
		int intMax = this.getIntMaxValue();
		int intValue = (int) Math.round(MathHelper.clampedLerp(intMin, intMax, pValue));
		return MathHelper.inverseLerp(intValue, intMin, intMax);
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
		int intMin = this.getIntMinValue();
		int intMax = this.getIntMaxValue();

		if (intMin == intMax)
		{
			return intMin;
		}
		else
		{
			return (int) Math.round(MathHelper.clampedLerp(intMin, intMax, this.getValue()));
		}

	}

	public void setIntValue(int intValue)
	{
		intValue = MathHelper.clamp(intValue, this.getIntMinValue(), this.getIntMaxValue());
		double doubleValue = MathHelper.inverseLerp(intValue, this.getIntMinValue(), this.getIntMaxValue());

		if (this.getIntValue() != intValue)
		{
			this.setValue(doubleValue);
		}
		else
		{
			this.setValueInternal(doubleValue);
		}

	}

	public int getIntMinValue()
	{
		return this.intMinValue;
	}

	public void setIntMinValue(int intMax)
	{
		if (this.getIntMinValue() != intMax)
		{
			this.intMinValue = intMax;
			this.onIntValueChanged();
		}

	}

	public int getIntMaxValue()
	{
		return this.intMaxValue;
	}

	public void setIntMaxValue(int intMax)
	{
		if (this.getIntMaxValue() != intMax)
		{
			this.intMaxValue = intMax;
			this.onIntValueChanged();
		}

	}

}
