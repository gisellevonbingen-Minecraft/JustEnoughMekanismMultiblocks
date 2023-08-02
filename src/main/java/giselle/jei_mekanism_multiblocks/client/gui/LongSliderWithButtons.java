package giselle.jei_mekanism_multiblocks.client.gui;

import net.minecraft.util.text.StringTextComponent;

public class LongSliderWithButtons extends SliderWithButtons<LongSliderWidget>
{
	public LongSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, long value, long min, long max)
	{
		this(pX, pY, pWidth, pHeight, translationKey, new LongSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, value, min, max));
	}

	public LongSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, LongSliderWidget slider)
	{
		super(pX, pY, pWidth, pHeight, translationKey, slider);
	}

	@Override
	protected String getDisplayValue()
	{
		return String.valueOf(this.getSlider().getValue());
	}

	@Override
	protected void onAdjustButtonPress(int delta)
	{
		super.onAdjustButtonPress(delta);

		LongSliderWidget slider = this.getSlider();
		long value = slider.getValue();
		slider.setValue(value + delta);
	}

}
