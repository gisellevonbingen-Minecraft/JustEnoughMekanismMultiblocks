package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import com.ibm.icu.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class LongSliderWithButtons extends ContainerWidget
{
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("+#;-#");

	private String translationKey;

	private final ButtonWidget minusButton;
	private final ButtonWidget plusButton;
	private final LongSliderWidget slider;

	public LongSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, long value, long min, long max)
	{
		this(pX, pY, pWidth, pHeight, translationKey, value, min, max, null);
	}

	public LongSliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, long value, long min, long max, LongConsumer setter)
	{
		super(pX, pY, pWidth, pHeight);
		this.translationKey = translationKey;

		this.addChild(this.minusButton = this.createAdjustButton(new StringTextComponent("-"), -1));
		this.addChild(this.plusButton = this.createAdjustButton(new StringTextComponent("+"), +1));
		this.addChild(this.slider = new LongSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, value, min, max, v ->
		{
			this.updateMessage();

			if (setter != null)
			{
				setter.accept(v);
			}
		}));
		this.updateMessage();
		this.onHeightChanged();
	}

	protected void updateMessage()
	{
		this.getSlider().setMessage(new TranslationTextComponent(this.translationKey, String.valueOf(this.getSlider().getLongValue())));
	}

	private ButtonWidget createAdjustButton(ITextComponent component, int direction)
	{
		int shiftDelta = 5;
		int normalDelta = 1;

		return new ButtonWidget(0, 0, 0, 0, component, b ->
		{
			long longValue = this.slider.getLongValue();
			long delta = Screen.hasShiftDown() ? shiftDelta : normalDelta;
			this.slider.setLongValue(longValue + delta * direction);
		}, (b, pMatrixStack, pMouseX, pMouseY) ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			List<ITextComponent> tooltips = new ArrayList<>();
			tooltips.add(new TranslationTextComponent("text.jei_mekanism_multiblocks.click.normal", DECIMAL_FORMAT.format(direction * normalDelta)));
			tooltips.add(new TranslationTextComponent("text.jei_mekanism_multiblocks.click.shift", DECIMAL_FORMAT.format(direction * shiftDelta)));
			GuiUtils.drawHoveringText(pMatrixStack, tooltips, pMouseX, pMouseY, minecraft.screen.width, minecraft.screen.height, -1, minecraft.font);
		});
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

		ButtonWidget minusButton = this.getMinusButton();
		minusButton.setWidth(height);
		minusButton.setHeight(height);

		ButtonWidget plusButton = this.getPlusButton();
		plusButton.setWidth(height);
		plusButton.setHeight(height);

		LongSliderWidget slider = this.getSlider();
		slider.setHeight(height);

		this.updateChildrenBounds();
	}

	protected void updateChildrenBounds()
	{
		ButtonWidget minusButton = this.getMinusButton();
		minusButton.x = 0;
		minusButton.y = 0;

		ButtonWidget plusButton = this.getPlusButton();
		plusButton.x = this.getWidth() - plusButton.getWidth();
		plusButton.y = minusButton.y;

		LongSliderWidget slider = this.getSlider();
		slider.x = minusButton.x + minusButton.getWidth();
		slider.y = minusButton.y;
		slider.setWidth(plusButton.x - slider.x);
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

	public LongSliderWidget getSlider()
	{
		return this.slider;
	}

}
