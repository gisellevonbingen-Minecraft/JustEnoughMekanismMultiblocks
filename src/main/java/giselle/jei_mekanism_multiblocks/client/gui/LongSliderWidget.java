package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LongSliderWidget extends SliderWidget
{
	private final List<LongConsumer> valueChangeHandlers;
	private long minValue;
	private long maxValue;
	private long prev;

	public LongSliderWidget(int x, int y, int width, int height, Component pMessage, long value, long min, long max)
	{
		super(x, y, width, height, pMessage, Mth.inverseLerp(value, min, max));
		this.valueChangeHandlers = new ArrayList<>();
		this.minValue = min;
		this.maxValue = max;
		this.prev = this.getValue();
	}

	public void addValueChangeHanlder(LongConsumer handler)
	{
		this.valueChangeHandlers.add(handler);
	}

	protected long toValue(double pRatio)
	{
		long minValue = this.getMinValue();
		long maxValue = this.getMaxValue();

		if (minValue == maxValue)
		{
			return maxValue;
		}
		else
		{
			return Math.round(Mth.clampedLerp(minValue, maxValue, pRatio));
		}

	}

	protected double toRatio(long value)
	{
		long minValue = this.getMinValue();
		long maxValue = this.getMaxValue();

		if (minValue == maxValue)
		{
			return 0.0D;
		}
		else
		{
			value = Math.min(Math.max(value, minValue), maxValue);
			return Mth.inverseLerp(value, minValue, maxValue);
		}

	}

	@Override
	protected double applyRatioFromMouse(double pRatio)
	{
		long value = this.toValue(pRatio);
		return this.toRatio(value);
	}

	@Override
	protected void onRatioChanged()
	{
		this.onValueDirty();

		super.onRatioChanged();
	}

	protected void onValueDirty()
	{
		long next = this.getValue();

		if (this.prev != next)
		{
			this.prev = next;
			this.onValueChanged();
		}

	}

	protected void onValueChanged()
	{
		long value = this.getValue();

		for (LongConsumer handler : this.valueChangeHandlers)
		{
			handler.accept(value);
		}

	}

	public long getValue()
	{
		return this.toValue(this.getRatio());
	}

	public void setValue(long value)
	{
		double ratio = this.toRatio(value);
		this.setRatio(ratio);
	}

	public long getMinValue()
	{
		return this.minValue;
	}

	public void setMinValue(long minValue)
	{
		if (this.getMinValue() != minValue)
		{
			this.minValue = minValue;
			this.onValueDirty();
		}

	}

	public long getMaxValue()
	{
		return this.maxValue;
	}

	public void setMaxValue(long maxValue)
	{
		if (this.getMaxValue() != maxValue)
		{
			this.maxValue = maxValue;
			this.onValueDirty();
		}

	}

}
