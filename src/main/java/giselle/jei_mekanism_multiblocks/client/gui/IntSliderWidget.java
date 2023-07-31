package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class IntSliderWidget extends SliderWidget
{
	private final List<IntConsumer> intValueChangeHandlers;
	private int intMinValue;
	private int intMaxValue;

	public IntSliderWidget(int x, int y, int width, int height, ITextComponent pMessage, int value, int min, int max)
	{
		super(x, y, width, height, pMessage, MathHelper.inverseLerp(value, min, max));
		this.intValueChangeHandlers = new ArrayList<>();
		this.intMinValue = min;
		this.intMaxValue = max;
	}

	public void addIntValueChangeHanlder(IntConsumer handler)
	{
		this.intValueChangeHandlers.add(handler);
	}

	protected int toIntValue(double pValue)
	{
		int intMin = this.getIntMinValue();
		int intMax = this.getIntMaxValue();

		if (intMin == intMax)
		{
			return intMax;
		}
		else
		{
			return (int) Math.round(MathHelper.clampedLerp(intMin, intMax, pValue));
		}

	}

	protected double fromIntValue(int intValue)
	{
		int intMin = this.getIntMinValue();
		int intMax = this.getIntMaxValue();

		if (intMin == intMax)
		{
			return 0.0D;
		}
		else
		{
			return MathHelper.inverseLerp(intValue, intMin, intMax);
		}

	}

	@Override
	protected double applyValueFromMouse(double pValue)
	{
		int intValue = this.toIntValue(pValue);
		return this.fromIntValue(intValue);
	}

	@Override
	protected void onValueChanged()
	{
		super.onValueChanged();

		this.onIntValueChanged();
	}

	private void onIntValueChanged()
	{
		int intValue = this.getIntValue();

		for (IntConsumer handler : this.intValueChangeHandlers)
		{
			handler.accept(intValue);
		}

	}

	public int getIntValue()
	{
		return this.toIntValue(this.getValue());
	}

	public void setIntValue(int intValue)
	{
		intValue = MathHelper.clamp(intValue, this.getIntMinValue(), this.getIntMaxValue());
		double doubleValue = this.fromIntValue(intValue);

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
