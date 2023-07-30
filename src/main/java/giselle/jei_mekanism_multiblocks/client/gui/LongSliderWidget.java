package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class LongSliderWidget extends SliderWidget
{
	private final List<LongConsumer> longValueChangeHandlers;
	private long longMinValue;
	private long longMaxValue;

	public LongSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, long value, long min, long max)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
		this.longValueChangeHandlers = new ArrayList<>();
		this.longMinValue = min;
		this.longMaxValue = max;
	}

	public void addLongValueChangeHanlder(LongConsumer handler)
	{
		this.longValueChangeHandlers.add(handler);
	}

	protected long toLongValue(double pValue)
	{
		long longMin = this.getLongMinValue();
		long longMax = this.getLongMaxValue();
		return Math.round(MathHelper.clampedLerp(longMin, longMax, pValue));
	}

	protected double fromLongValue(long longValue)
	{
		long longMin = this.getLongMinValue();
		long longMax = this.getLongMaxValue();
		return MathHelper.inverseLerp(longValue, longMin, longMax);
	}

	@Override
	protected double applyValueFromMouse(double pValue)
	{
		long longValue = this.toLongValue(pValue);
		return this.fromLongValue(longValue);
	}

	@Override
	protected void onValueChanged()
	{
		super.onValueChanged();

		this.onLongValueChanged();
	}

	private void onLongValueChanged()
	{
		long longValue = this.getLongValue();

		for (LongConsumer handler : this.longValueChangeHandlers)
		{
			handler.accept(longValue);
		}

	}

	public long getLongValue()
	{
		return this.toLongValue(this.getValue());
	}

	public void setLongValue(long longValue)
	{
		longValue = MathHelper.clamp(longValue, this.getLongMinValue(), this.getLongMaxValue());
		double doubleValue = this.fromLongValue(longValue);

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
