package giselle.jei_mekanism_multiblocks.common.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DurationTextHelper
{
	private static final TextComponent DELIMITER = new StringTextComponent(" ");

	public static IFormattableTextComponent ticks(long ticks)
	{
		return new TranslationTextComponent("text.jei_mekanism_multiblocks.duration.ticks", TextUtils.format(ticks));
	}

	public static IFormattableTextComponent duration(long ticks)
	{
		if (ticks <= 0L)
		{
			return ticks(ticks);
		}

		List<IFormattableTextComponent> components = new ArrayList<>();
		ComponentAccumulator add = (translationKey, value) ->
		{
			if (value <= 0L)
			{
				return;
			}

			String format = "";

			if (!components.isEmpty())
			{
				components.add(DELIMITER);
				format = "%02d";
			}
			else
			{
				format = "%01d";
			}

			components.add(new TranslationTextComponent(translationKey, String.format(format, value)));
		};

		Duration duration = Duration.ofSeconds(ticks / 20);
		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;
		ticks = ticks % 20;

		add.accept("text.jei_mekanism_multiblocks.duration.days", days);
		add.accept("text.jei_mekanism_multiblocks.duration.hours", hours);
		add.accept("text.jei_mekanism_multiblocks.duration.minutes", minutes);
		add.accept("text.jei_mekanism_multiblocks.duration.seconds", seconds);
		add.accept("text.jei_mekanism_multiblocks.duration.ticks", ticks);

		return components.stream().reduce((IFormattableTextComponent) new StringTextComponent(""), IFormattableTextComponent::append, IFormattableTextComponent::append);
	}

	private DurationTextHelper()
	{

	}

	@FunctionalInterface
	private interface ComponentAccumulator
	{
		void accept(String translationKey, long value);
	}

}
