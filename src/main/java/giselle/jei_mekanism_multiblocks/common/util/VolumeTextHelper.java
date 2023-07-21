package giselle.jei_mekanism_multiblocks.common.util;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class VolumeTextHelper
{
	private static final Map<Integer, VolumeUnit> MULTIPLIER_TO_UNIT = new HashMap<>();

	static
	{
		for (VolumeUnit unit : VolumeUnit.values())
		{
			MULTIPLIER_TO_UNIT.put(unit.getMultiplier(), unit);
		}

	}

	public static ITextComponent formatMilliBuckets(double value)
	{
		return format(value, VolumeUnit.MILLI, "B");
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

		String text = TextUtils.format(remain);

		if (multiplier > -1)
		{
			int decimalIndex = text.indexOf('.');
			boolean hasDecimalPart = decimalIndex > -1;
			String exponentialPart = hasDecimalPart ? text.substring(0, decimalIndex) : text;
			String deciamlPart = hasDecimalPart ? text.substring(decimalIndex + 1) : "";
			int currentDecimals = hasDecimalPart ? deciamlPart.length() : 0;

			if (currentDecimals > decimals)
			{
				deciamlPart = deciamlPart.substring(0, decimals);
			}
			else if (currentDecimals < decimals)
			{
				StringBuilder zero = new StringBuilder();

				for (int i = 0; i < decimals - currentDecimals; i++)
				{
					zero.append('0');
				}

				deciamlPart += zero;
			}

			text = new StringBuilder().append(exponentialPart).append(".").append(deciamlPart).toString();
		}

		return new StringTextComponent(new StringBuilder().append(text).append(" ").append(to.getShortName()).append(unit).toString());
	}

	private VolumeTextHelper()
	{

	}

}
