package giselle.jei_mekanism_multiblocks.common.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DurationTextHelper
{
	private static final TextComponent DELIMITER = new StringTextComponent(" ");

	public static TextComponent ticks(long ticks)
	{
		return new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.ticks", TextUtils.format(ticks));
	}

	public static IFormattableTextComponent duration(long ticks)
	{
		List<TextComponent> components = new ArrayList<>();
		Consumer<TextComponent> add = c ->
		{
			if (!components.isEmpty())
			{
				components.add(DELIMITER);
			}

			components.add(c);
		};

		Duration duration = Duration.ofSeconds(ticks / 20);
		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;
		ticks = ticks % 20;

		if (days > 0)
		{
			add.accept(new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.days", days));
		}

		if (hours > 0)
		{
			add.accept(new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.hours", String.format("%02d", hours)));
		}

		if (minutes > 0)
		{
			add.accept(new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.minutes", String.format("%02d", minutes)));
		}

		if (seconds > 0)
		{
			add.accept(new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.seconds", String.format("%02d", seconds)));
		}

		if (ticks > 0)
		{
			add.accept(new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.ticks", String.format("%02d", ticks)));
		}

		return components.stream().reduce((IFormattableTextComponent) new StringTextComponent(""), IFormattableTextComponent::append, IFormattableTextComponent::append);
	}

	private DurationTextHelper()
	{

	}

}
