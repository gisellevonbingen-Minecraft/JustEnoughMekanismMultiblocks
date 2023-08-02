package giselle.jei_mekanism_multiblocks.client.gui;

import com.ibm.icu.text.DecimalFormat;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class SliderWithButtons<SLIDER extends SliderWidget> extends ContainerWidget
{
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("+#;-#");

	private String translationKey;

	private final SLIDER slider;
	private final ButtonWidget minusButton;
	private final ButtonWidget plusButton;

	public SliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, SLIDER slider)
	{
		super(pX, pY, pWidth, pHeight);
		this.translationKey = translationKey;

		this.addChild(this.slider = slider);
		this.slider.addRatioChangeHanlder(v ->
		{
			this.updateMessage();
		});
		this.addChild(this.minusButton = this.createAdjustButton(new StringTextComponent("-"), -1));
		this.addChild(this.plusButton = this.createAdjustButton(new StringTextComponent("+"), +1));

		this.updateMessage();
		this.onHeightChanged();
	}

	protected abstract String getDisplayValue();

	protected void updateMessage()
	{
		this.getSlider().setMessage(new TranslationTextComponent(this.translationKey, this.getDisplayValue()));
	}

	private ButtonWidget createAdjustButton(ITextComponent component, int direction)
	{
		int shiftDelta = 5;
		int normalDelta = 1;

		ButtonWidget button = new ButtonWidget(0, 0, 0, 0, component);
		button.setTooltip(//
				new TranslationTextComponent("text.jei_mekanism_multiblocks.click.normal", DECIMAL_FORMAT.format(direction * normalDelta)), //
				new TranslationTextComponent("text.jei_mekanism_multiblocks.click.shift", DECIMAL_FORMAT.format(direction * shiftDelta)));
		button.addPressHandler(b ->
		{
			int delta = Screen.hasShiftDown() ? shiftDelta : normalDelta;
			this.onAdjustButtonPress(delta * direction);
		});
		return button;
	}

	protected void onAdjustButtonPress(int delta)
	{

	}

	@Override
	protected void onWidthChanged()
	{
		super.onWidthChanged();

		this.updateChildrenBounds();
	}

	@Override
	protected void onHeightChanged()
	{
		super.onHeightChanged();

		int height = this.getHeight();

		SLIDER slider = this.getSlider();
		slider.setHeight(height);

		ButtonWidget minusButton = this.getMinusButton();
		minusButton.setWidth(height);
		minusButton.setHeight(height);

		ButtonWidget plusButton = this.getPlusButton();
		plusButton.setWidth(height);
		plusButton.setHeight(height);

		this.updateChildrenBounds();
	}

	protected void updateChildrenBounds()
	{
		ButtonWidget plusButton = this.getPlusButton();
		plusButton.x = this.getWidth() - plusButton.getWidth();
		plusButton.y = 0;

		ButtonWidget minusButton = this.getMinusButton();
		minusButton.x = plusButton.x - minusButton.getWidth();
		minusButton.y = plusButton.y;

		SLIDER slider = this.getSlider();
		slider.x = 0;
		slider.y = minusButton.y;
		slider.setWidth(minusButton.x - slider.x);
	}

	public void setTranslationKey(String translationKey)
	{
		this.translationKey = translationKey;
		this.updateMessage();
	}

	public String getTranslationKey()
	{
		return this.translationKey;
	}

	public ButtonWidget getMinusButton()
	{
		return this.minusButton;
	}

	public ButtonWidget getPlusButton()
	{
		return this.plusButton;
	}

	public SLIDER getSlider()
	{
		return this.slider;
	}

}
