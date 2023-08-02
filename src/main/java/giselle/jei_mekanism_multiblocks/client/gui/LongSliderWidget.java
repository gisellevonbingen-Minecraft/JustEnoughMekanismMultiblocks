package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class LongSliderWidget extends SliderWidget
{
	private final List<LongConsumer> valueChangeHandlers;
	private long minValue;
	private long maxValue;
	private long prev;

	public LongSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, long value, long min, long max)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
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
			return Math.round(MathHelper.clampedLerp(minValue, maxValue, pRatio));
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
			value = MathHelper.clamp(value, minValue, maxValue);
			return MathHelper.inverseLerp(value, minValue, maxValue);
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
		long next = this.getValue();

		if (this.prev != next)
		{
			this.prev = next;
			this.onValueChanged();
		}

		super.onRatioChanged();
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
			this.onValueChanged();
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
			this.onValueChanged();
		}

	}

}
