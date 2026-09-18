package giselle.jei_mekanism_multiblocks.common.util;

import java.text.NumberFormat;
import java.util.Locale;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class VolumeTextHelper
{
	private static final Int2ObjectMap<VolumeUnit> MULTIPLIER_TO_UNIT = new Int2ObjectOpenHashMap<>();
	private static final Int2ObjectMap<NumberFormat> DECIMALS_TO_FORMAT = new Int2ObjectOpenHashMap<>();

	static
	{
		for (VolumeUnit unit : VolumeUnit.values())
		{
			MULTIPLIER_TO_UNIT.put(unit.getMultiplier(), unit);
		}

	}

	public static ITextComponent formatMB(double value)
	{
		return format(value, VolumeUnit.MILLI, "B");
	}

	public static ITextComponent formatMBt(double value)
	{
		return format(value, VolumeUnit.MILLI, "B/t");
	}

	public static ITextComponent format(double value, VolumeUnit from, String unit)
	{
		return format(value, from, unit, 3);
	}

	public static ITextComponent format(double value, VolumeUnit from, String unit, int decimals)
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
		return new StringTextComponent(new StringBuilder().append(text).append(" ").append(to.getShortName()).append(unit).toString());
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
