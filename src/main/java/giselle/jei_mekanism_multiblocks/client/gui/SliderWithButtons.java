package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.ibm.icu.text.DecimalFormat;

import giselle.jei_mekanism_multiblocks.client.mixin.minecraft.TooltipAccessor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class SliderWithButtons<SLIDER extends SliderWidget> extends ContainerWidget
{
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("+#;-#");
	public static int SHIFT_DELTA = 5;
	public static int NORMAL_DELTA = 1;

	private String translationKey;

	private final SLIDER slider;
	private final Map<ButtonWidget, Integer> button2DirectionMap;
	private final ButtonWidget minusButton;
	private final ButtonWidget plusButton;

	public SliderWithButtons(int pX, int pY, int pWidth, int pHeight, String translationKey, SLIDER slider)
	{
		super(pX, pY, pWidth, pHeight);
		this.translationKey = translationKey;

		this.addChild(this.slider = slider);
		this.button2DirectionMap = new HashMap<>();
		this.minusButton = this.createAdjustButton(Component.literal("-"), -1);
		this.plusButton = this.createAdjustButton(Component.literal("+"), +1);

		this.updateMessage();
		this.onHeightChanged();
	}

	@Override
	public void setTooltip(@Nullable Tooltip pTooltip)
	{
		this.getSlider().setTooltip(pTooltip);

		for (Entry<ButtonWidget, Integer> entry : this.button2DirectionMap.entrySet())
		{
			this.updateAdjustButtonTooltip(entry.getKey(), entry.getValue());
		}

	}

	protected abstract String getDisplayValue();

	protected void updateMessage()
	{
		this.getSlider().setMessage(Component.translatable(this.translationKey, this.getDisplayValue()));
	}

	private ButtonWidget createAdjustButton(Component message, int direction)
	{
		ButtonWidget button = new ButtonWidget(0, 0, 0, 0, message);
		this.updateAdjustButtonTooltip(button, direction);
		button.addPressHandler(b ->
		{
			int delta = Screen.hasShiftDown() ? SHIFT_DELTA : NORMAL_DELTA;
			this.onAdjustButtonPress(delta * direction);
		});
		this.button2DirectionMap.put(button, direction);
		this.addChild(button);
		return button;
	}

	private void updateAdjustButtonTooltip(ButtonWidget button, int direction)
	{
		MutableComponent component = Component.empty();
		Component narration = null;
		Tooltip tooltip = this.getSlider().getTooltip();

		if (tooltip != null)
		{
			TooltipAccessor accessor = (TooltipAccessor) tooltip;
			component.append(accessor.getMessage()).append("\n");
			narration = accessor.getNarration();
		}

		component.append(Component.translatable("text.jei_mekanism_multiblocks.tooltip.click_normal", DECIMAL_FORMAT.format(direction * NORMAL_DELTA))).append("\n");
		component.append(Component.translatable("text.jei_mekanism_multiblocks.tooltip.click_shift", DECIMAL_FORMAT.format(direction * SHIFT_DELTA)));
		button.setTooltip(Tooltip.create(component, narration));
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
		plusButton.setX(this.getWidth() - plusButton.getWidth());
		plusButton.setY(0);

		ButtonWidget minusButton = this.getMinusButton();
		minusButton.setX(plusButton.getX() - minusButton.getWidth());
		minusButton.setY(plusButton.getY());

		SLIDER slider = this.getSlider();
		slider.setX(0);
		slider.setY(minusButton.getY());
		slider.setWidth(minusButton.getX() - slider.getX());
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
