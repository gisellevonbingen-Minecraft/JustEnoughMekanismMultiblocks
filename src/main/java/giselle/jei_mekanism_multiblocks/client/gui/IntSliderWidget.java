package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class IntSliderWidget extends SliderWidget
{
	private final List<IntConsumer> valueChangeHandlers;
	private int minValue;
	private int maxValue;
	private int prev;

	public IntSliderWidget(int x, int y, int width, int height, Component pMessage, int value, int min, int max)
	{
		super(x, y, width, height, pMessage, Mth.inverseLerp(value, min, max));
		this.valueChangeHandlers = new ArrayList<>();
		this.minValue = min;
		this.maxValue = max;
		this.prev = this.getValue();
	}

	public void addValueChangeHanlder(IntConsumer handler)
	{
		this.valueChangeHandlers.add(handler);
	}

	protected int toValue(double pRatio)
	{
		int minValue = this.getMinValue();
		int maxValue = this.getMaxValue();

		if (minValue == maxValue)
		{
			return maxValue;
		}
		else
		{
			return (int) Math.round(Mth.clampedLerp(minValue, maxValue, pRatio));
		}

	}

	protected double toRatio(int value)
	{
		int minValue = this.getMinValue();
		int maxValue = this.getMaxValue();

		if (minValue == maxValue)
		{
			return 0.0D;
		}
		else
		{
			value = Mth.clamp(value, minValue, maxValue);
			return Mth.inverseLerp(value, minValue, maxValue);
		}

	}

	@Override
	protected double applyRatioFromMouse(double pRatio)
	{
		int value = this.toValue(pRatio);
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
		int next = this.getValue();

		if (this.prev != next)
		{
			this.prev = next;
			this.onValueChanged();
		}

	}

	protected void onValueChanged()
	{
		int value = this.getValue();

		for (IntConsumer handler : this.valueChangeHandlers)
		{
			handler.accept(value);
		}

	}

	public int getValue()
	{
		return this.toValue(this.getRatio());
	}

	public void setValue(int value)
	{
		double ratio = this.toRatio(value);
		this.setRatio(ratio);
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
			this.onValueDirty();
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
			this.onValueDirty();
		}

	}

}
