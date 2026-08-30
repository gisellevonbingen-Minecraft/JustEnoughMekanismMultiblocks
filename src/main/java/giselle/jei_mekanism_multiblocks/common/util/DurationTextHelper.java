package giselle.jei_mekanism_multiblocks.common.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
		List<Component> components = new ArrayList<>();
		Consumer<Component> add = c ->
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
		long seconds = duration.toSeconds() % 60;
		ticks = ticks % 20;

		if (days > 0)
		{
			add.accept(Component.translatable("text.jei_mekanism_multiblocks.duration.days", days));
		}

		if (hours > 0)
		{
			add.accept(Component.translatable("text.jei_mekanism_multiblocks.duration.hours", String.format("%02d", hours)));
		}

		if (minutes > 0)
		{
			add.accept(Component.translatable("text.jei_mekanism_multiblocks.duration.minutes", String.format("%02d", minutes)));
		}

		if (seconds > 0)
		{
			add.accept(Component.translatable("text.jei_mekanism_multiblocks.duration.seconds", String.format("%02d", seconds)));
		}

		if (ticks > 0)
		{
			add.accept(Component.translatable("text.jei_mekanism_multiblocks.duration.ticks", String.format("%02d", ticks)));
		}

		return components.stream().reduce(Component.empty(), MutableComponent::append, MutableComponent::append);
	}

	private DurationTextHelper()
	{

	}

}
