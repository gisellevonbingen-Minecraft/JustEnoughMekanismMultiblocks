package giselle.jei_mekanism_multiblocks.common.util;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;

public class VolumeTextHelper
{
	private static final Map<Integer, VolumeUnit> MULTIPLIER_TO_UNIT = new HashMap<>();
	private static final Map<Integer, NumberFormat> DECIMALS_TO_FORMAT = new HashMap<>();

	static
	{
		for (VolumeUnit unit : VolumeUnit.values())
		{
			MULTIPLIER_TO_UNIT.put(unit.getMultiplier(), unit);
		}

	}

	public static Component formatMB(double value)
	{
		return format(value, VolumeUnit.MILLI, "B");
	}

	public static Component formatMBt(double value)
	{
		return format(value, VolumeUnit.MILLI, "B/t");
	}

	public static Component format(double value, VolumeUnit from, String unit)
	{
		return format(value, from, unit, 3);
	}

	public static Component format(double value, VolumeUnit from, String unit, int decimals)
	{
		VolumeUnit to = from;
		int multiplier = from.getMultiplier();
		double remain = value;

		while (remain >= 1000.0D)
		{
			VolumeUnit next = MULTIPLIER_TO_UNIT.get(multiplier + 1);

			if (next == null)
			{
				break;
			}
			else
			{
				multiplier++;
				remain /= 1000.0D;
				to = next;
			}

		}

		String text = formatDecimals(remain, decimals, multiplier > -1);
		return Component.literal(new StringBuilder().append(text).append(" ").append(to.getShortName()).append(unit).toString());
	}

	private static String formatDecimals(double value, int decimals, boolean fixedDigits)
	{
		if (fixedDigits)
		{
			return DECIMALS_TO_FORMAT.computeIfAbsent(decimals, d ->
			{
				NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
				format.setGroupingUsed(false);
				format.setMinimumFractionDigits(d);
				format.setMaximumFractionDigits(d);
				return format;
			}).format(value);
		}
		else
		{
			return TextUtils.format(value);
		}

	}

	private VolumeTextHelper()
	{

	}

}
