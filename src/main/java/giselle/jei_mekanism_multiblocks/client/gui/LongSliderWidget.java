package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.LongConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class LongSliderWidget extends SliderWidget
{
	private final LongConsumer setter;
	private long longMinValue;
	private long longMaxValue;

	public LongSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, long value, long min, long max)
	{
		this(x, y, width, height, pMessage, value, min, max, null);
	}

	public LongSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, long value, long min, long max, LongConsumer setter)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
		this.longMinValue = min;
		this.longMaxValue = max;
		this.setter = setter;
	}

	@Override
	protected double applyValueFromMouse(double pValue)
	{
		long longMin = this.getLongMinValue();
		long longMax = this.getLongMaxValue();
		long longValue = (long) Math.round(MathHelper.clampedLerp(longMin, longMax, pValue));
		return MathHelper.inverseLerp(longValue, longMin, longMax);
	}

	@Override
	protected void onValueChanged()
	{
		super.onValueChanged();

		this.onLongValueChanged();
	}

	private void onLongValueChanged()
	{
		this.setter.accept(this.getLongValue());
	}

	public long getLongValue()
	{
		long longMin = this.getLongMinValue();
		long longMax = this.getLongMaxValue();

		if (longMin == longMax)
		{
			return longMin;
		}
		else
		{
			return (long) Math.round(MathHelper.clampedLerp(longMin, longMax, this.getValue()));
		}

	}

	public void setLongValue(long longValue)
	{
		longValue = MathHelper.clamp(longValue, this.getLongMinValue(), this.getLongMaxValue());
		double doubleValue = MathHelper.inverseLerp(longValue, this.getLongMinValue(), this.getLongMaxValue());

		if (this.getLongValue() != longValue)
		{
			this.setValue(doubleValue);
		}
		else
		{
			this.setValueInternal(doubleValue);
		}

	}

	public long getLongMinValue()
	{
		return this.longMinValue;
	}

	public void setLongMinValue(long longMax)
	{
		if (this.getLongMinValue() != longMax)
		{
			this.longMinValue = longMax;
			this.onLongValueChanged();
		}

	}

	public long getLongMaxValue()
	{
		return this.longMaxValue;
	}

	public void setLongMaxValue(long longMax)
	{
		if (this.getLongMaxValue() != longMax)
		{
			this.longMaxValue = longMax;
			this.onLongValueChanged();
		}

	}

}
