package giselle.jei_mekanism_multiblocks.client.gui;

import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.StringTextComponent;

public class IntSliderWithButtons extends SliderWithButtons<IntSliderWidget>
{
	public IntSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, int value, int min, int max)
	{
		this(pX, pY, pWidth, pHeight, translationKey, new IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, value, min, max));
	}

	public IntSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, IntSliderWidget slider)
	{
		super(pX, pY, pWidth, pHeight, translationKey, slider);
		slider.addValueChangeHanlder(v ->
		{
			this.updateMessage();
		});
	}

	@Override
	protected String getDisplayValue()
	{
		return TextUtils.format(this.getSlider().getValue());
	}

	@Override
	protected void onAdjustButtonPress(int delta)
	{
		super.onAdjustButtonPress(delta);

		IntSliderWidget slider = this.getSlider();
		int value = slider.getValue();
		slider.setValue(value + delta);
	}

}
