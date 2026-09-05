package giselle.jei_mekanism_multiblocks.common.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DurationTextHelper
{
	private static final Component DELIMITER = Component.literal(" ");

	public static Component ticks(long ticks)
	{
		return Component.translatable("text.jei_mekanism_multiblocks.duration.ticks", TextUtils.format(ticks));
	}

	public static Component duration(long ticks)
	{
		if (ticks <= 0L)
		{
			return ticks(ticks);
		}

		List<Component> components = new ArrayList<>();
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

			components.add(Component.translatable(translationKey, String.format(format, value)));
		};

		Duration duration = Duration.ofSeconds(ticks / 20);
		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.toSeconds() % 60;
		ticks = ticks % 20;

		add.accept("text.jei_mekanism_multiblocks.duration.days", days);
		add.accept("text.jei_mekanism_multiblocks.duration.hours", hours);
		add.accept("text.jei_mekanism_multiblocks.duration.minutes", minutes);
		add.accept("text.jei_mekanism_multiblocks.duration.seconds", seconds);
		add.accept("text.jei_mekanism_multiblocks.duration.ticks", ticks);

		return components.stream().reduce(Component.empty(), MutableComponent::append, MutableComponent::append);
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
