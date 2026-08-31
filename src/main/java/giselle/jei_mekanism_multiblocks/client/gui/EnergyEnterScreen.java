package giselle.jei_mekanism_multiblocks.client.gui;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EnergyEnterScreen extends Screen
{
	private static final Component COMPONENT_ENTER = Component.translatable("text.jei_mekanism_multiblocks.enter");
	private static final Component COMPONENT_EXAMPLE = Component.translatable("text.jei_mekanism_multiblocks.example");
	private static final Component COMPONENT_EXAMPLES = Component.literal("1000, 100_000, 10K, 400MFE, 1GJ");

	private final Consumer<FloatingLong> doneHandler;

	private int boxWidth;
	private int boxHeight;
	private int boxX;
	private int boxY;
	private EditBox editBox;
	private String lastText;

	private EnergyUnit displayUnit;
	private FloatingLong jules;

	private boolean julesChanged;
	private FloatingLong lastJules;
	private EnergyUnit lastUnit;
	private Component displayText;

	public EnergyEnterScreen(Component title, Consumer<FloatingLong> doneHandler)
	{
		super(title);
		this.doneHandler = doneHandler;

		this.jules = FloatingLong.ZERO;
		this.displayUnit = EnergyUnit.getConfigured();
	}

	@Override
	protected void init()
	{
		super.init();

		this.boxWidth = 180;
		this.boxHeight = 120;

		this.boxX = (this.width - this.boxWidth) / 2;
		this.boxY = (this.height - this.boxHeight) / 2;

		this.editBox = new EditBox(this.font, this.boxX, this.boxY + this.font.lineHeight, this.boxWidth, 20, this.editBox, Component.empty());
		this.addRenderableWidget(this.editBox);
		this.setFocused(this.editBox);

		ButtonWidget doneButton = new ButtonWidget(this.editBox.getX() + this.editBox.getWidth() - 40, this.editBox.getY() + this.editBox.getHeight() + 2, 40, 20, COMPONENT_ENTER);
		doneButton.addPressHandler($ -> this.onDone());
		this.addRenderableWidget(doneButton);
	}

	@Override
	public void tick()
	{
		super.tick();

		this.displayUnit = EnergyUnit.getConfigured();

		if (this.julesChanged)
		{
			this.julesChanged = false;
			this.editBox.setValue(Component.translatable("%s %s", this.displayUnit.convertTo(this.jules), Component.translatable(this.displayUnit.getTranslationKey())).getString());
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
		EnergyUnit energyUnit = this.displayUnit;
		VolumeUnit volumeUnit = VolumeUnit.ONE;
		text = text.replaceAll("[\\s_]", "");

		for (EnergyUnit unit : EnergyUnit.values())
		{
			if (!unit.isEnabled())
			{
				continue;
			}

			String tabName = unit.getTabName();

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

		this.jules = energyUnit.convertFrom(parsedValue);
		return true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(guiGraphics);

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		if (this.displayText == null || !this.lastJules.equals(this.jules) || this.lastUnit != this.displayUnit)
		{
			this.lastJules = this.jules;
			this.lastUnit = this.displayUnit;
			this.displayText = Component.translatable("=> %s", UnitDisplayUtils.getDisplayShort(this.displayUnit.convertTo(this.jules), this.displayUnit));
		}

		int titleWidth = this.font.width(this.title);
		guiGraphics.drawString(this.font, this.title, this.editBox.getX() + (this.editBox.getWidth() - titleWidth) / 2, this.editBox.getY() - this.font.lineHeight - 4, 0xFFFFFFFF, true);

		int textX = this.editBox.getX();
		int textY = this.editBox.getY() + this.editBox.getHeight() + 2;
		guiGraphics.drawString(this.font, this.displayText, textX, textY, 0xFFFFFFFF, true);

		int exampleX = this.boxX;
		int exampleY = this.boxY + this.boxHeight - this.font.lineHeight * 2;
		guiGraphics.drawString(this.font, COMPONENT_EXAMPLE, exampleX, exampleY, 0xFFFFFFFF, true);
		guiGraphics.drawString(this.font, COMPONENT_EXAMPLES, exampleX + 5, exampleY + this.font.lineHeight, 0xFFFFFFFF, true);
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
		if (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER)
		{
			this.onDone();
			return true;
		}

		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if (pButton == InputConstants.MOUSE_BUTTON_RIGHT)
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
