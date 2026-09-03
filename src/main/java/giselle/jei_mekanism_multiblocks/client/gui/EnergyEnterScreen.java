package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyEnterScreen extends Screen
{
	private static final ITextComponent COMPONENT_ENTER = new TranslationTextComponent("text.jei_mekanism_multiblocks.enter");
	private static final ITextComponent COMPONENT_EXAMPLE = new TranslationTextComponent("text.jei_mekanism_multiblocks.example");
	private static final ITextComponent COMPONENT_EXAMPLES = new StringTextComponent("1000, 100_000, 10K, 400MFE, 1GJ");

	private final Consumer<FloatingLong> doneHandler;

	private int boxWidth;
	private int boxHeight;
	private int boxX;
	private int boxY;
	private TextFieldWidget editBox;
	private String lastText;

	private EnergyType displayUnit;
	private FloatingLong jules;

	private boolean julesChanged;
	private FloatingLong lastJules;
	private EnergyType lastUnit;
	private ITextComponent displayText;

	public EnergyEnterScreen(ITextComponent title, Consumer<FloatingLong> doneHandler)
	{
		super(title);
		this.doneHandler = doneHandler;

		this.jules = FloatingLong.ZERO;
		this.displayUnit = MekanismConfig.general.energyUnit.get();
	}

	@Override
	protected void init()
	{
		super.init();

		this.boxWidth = 180;
		this.boxHeight = 120;

		this.boxX = (this.width - this.boxWidth) / 2;
		this.boxY = (this.height - this.boxHeight) / 2;

		this.editBox = new TextFieldWidget(this.font, this.boxX, this.boxY + this.font.lineHeight, this.boxWidth, 20, this.editBox, StringTextComponent.EMPTY);
		this.addButton(this.editBox);
		this.setFocused(this.editBox);
		this.editBox.setFocus(true);

		ButtonWidget doneButton = new ButtonWidget(this.editBox.x + this.editBox.getWidth() - 40, this.editBox.y + this.editBox.getHeight() + 2, 40, 20, COMPONENT_ENTER);
		doneButton.addPressHandler($ -> this.onDone());
		this.addButton(doneButton);
	}

	@Override
	public void tick()
	{
		super.tick();

		this.displayUnit = MekanismConfig.general.energyUnit.get();

		if (this.julesChanged)
		{
			this.julesChanged = false;
			this.editBox.setValue(new TranslationTextComponent("%s %s", MekanismUtils.convertToDisplay(this.jules), new TranslationTextComponent(this.displayUnit.getTranslationKey())).getString());
			this.editBox.moveCursorToEnd();
			this.editBox.setHighlightPos(0);
		}

		this.updateJules();
	}

	private boolean updateJules()
	{
		String text = this.editBox.getValue();

		if (this.lastText != null && this.lastText.equals(text))
		{
			return false;
		}

		this.lastText = text;
		EnergyType energyUnit = this.displayUnit;
		VolumeUnit volumeUnit = VolumeUnit.ONE;
		text = text.replaceAll("[\\s_]", "");

		for (EnergyType unit : EnergyType.values())
		{
			String tabName = unit.toString();

			if (text.length() < tabName.length())
			{
				continue;
			}

			String substring = text.substring(text.length() - tabName.length());

			if (substring.equalsIgnoreCase(tabName))
			{
				energyUnit = unit;
				text = text.substring(0, text.length() - tabName.length());
				break;
			}

		}

		for (VolumeUnit unit : VolumeUnit.values())
		{
			if (unit.getMultiplier() <= 0)
			{
				continue;
			}

			String shortName = unit.getShortName();

			if (text.length() < shortName.length())
			{
				continue;
			}

			String substring = text.substring(text.length() - shortName.length());

			if (substring.equalsIgnoreCase(shortName))
			{
				volumeUnit = unit;
				text = text.substring(0, text.length() - shortName.length());
				break;
			}

		}

		FloatingLong parsedValue = null;

		try
		{
			FloatingLong raw = FloatingLong.parseFloatingLong(text);
			parsedValue = raw.multiply(Math.pow(1000.0D, volumeUnit.getMultiplier()));
		}
		catch (Exception e)
		{
			parsedValue = FloatingLong.ZERO;
		}

		if (energyUnit == EnergyType.J)
		{
			this.jules = parsedValue;
		}
		else
		{
			mekanism.common.integration.energy.EnergyCompatUtils.EnergyType converting = null;

			if (energyUnit == EnergyType.FE)
			{
				converting = mekanism.common.integration.energy.EnergyCompatUtils.EnergyType.FORGE;
			}
			else if (energyUnit == EnergyType.EU)
			{
				converting = mekanism.common.integration.energy.EnergyCompatUtils.EnergyType.EU;
			}

			this.jules = converting.convertFrom(parsedValue);
		}

		return true;
	}

	@Override
	public void render(MatrixStack pMatrixStack, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(pMatrixStack);

		super.render(pMatrixStack, mouseX, mouseY, partialTick);

		if (this.displayText == null || !this.lastJules.equals(this.jules) || this.lastUnit != this.displayUnit)
		{
			this.lastJules = this.jules;
			this.lastUnit = this.displayUnit;
			this.displayText = new TranslationTextComponent("=> %s", MekanismUtils.getEnergyDisplayShort(this.jules));
		}

		int titleWidth = this.font.width(this.title);
		this.font.drawShadow(pMatrixStack, this.title, this.editBox.x + (this.editBox.getWidth() - titleWidth) / 2, this.editBox.y - this.font.lineHeight - 4, 0xFFFFFFFF);

		int textX = this.editBox.x;
		int textY = this.editBox.y + this.editBox.getHeight() + 2;
		this.font.drawShadow(pMatrixStack, this.displayText, textX, textY, 0xFFFFFFFF);

		int exampleX = this.boxX;
		int exampleY = this.boxY + this.boxHeight - this.font.lineHeight * 2;
		this.font.drawShadow(pMatrixStack, COMPONENT_EXAMPLE, exampleX, exampleY, 0xFFFFFFFF);
		this.font.drawShadow(pMatrixStack, COMPONENT_EXAMPLES, exampleX + 5, exampleY + this.font.lineHeight, 0xFFFFFFFF);
	}

	protected void onDone()
	{
		if (this.doneHandler != null)
		{
			this.doneHandler.accept(this.jules);
		}

		this.onClose();
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
	{
		if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_KP_ENTER)
		{
			this.onDone();
			return true;
		}

		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
		{
			if (this.editBox.isMouseOver(pMouseX, pMouseY))
			{
				this.editBox.setValue("");
				return true;
			}

		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	public void setJules(FloatingLong jules)
	{
		this.jules = jules == null ? FloatingLong.ZERO : jules.copy();
		this.julesChanged = true;
	}

	public FloatingLong getJules()
	{
		return this.jules.copy();
	}

}
