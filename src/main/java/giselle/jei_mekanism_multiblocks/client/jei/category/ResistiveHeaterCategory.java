package giselle.jei_mekanism_multiblocks.client.jei.category;

import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.Direction;

public class ResistiveHeaterCategory
{
	public static final double HEAT_CAPACITY = 100.0D;
	public static final double INVERSE_CONDUCTION_COEFFICIENT = 5.0D;
	public static final double INVERSE_INSULATION_COEFFICIENT = 100.0D;

	public static double getStableTemp(double heat)
	{
		double tempTarget = heat / HEAT_CAPACITY;
		double inverseConduction = HeatAPI.DEFAULT_INVERSE_CONDUCTION + INVERSE_CONDUCTION_COEFFICIENT;

		return (tempTarget * inverseConduction) + HeatAPI.AMBIENT_TEMP;
	}

	public static double getEnvironmentLossHeat(double temp)
	{
		double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + INVERSE_INSULATION_COEFFICIENT + INVERSE_CONDUCTION_COEFFICIENT;
		double tempToTransfer = (temp - HeatAPI.AMBIENT_TEMP) / invConduction;
		return Direction.values().length * tempToTransfer * HEAT_CAPACITY;
	}

	public static FloatingLong getHeatTransferableEnergy(double heat)
	{
		double stableTemp = ResistiveHeaterCategory.getStableTemp(heat);
		double envLossHeat = ResistiveHeaterCategory.getEnvironmentLossHeat(stableTemp);
		return FloatingLong.create(ResistiveHeaterCategory.getEnergyForHeat(heat + envLossHeat));
	}

	public static double getEnergyForHeat(double heat)
	{
		return heat / MekanismConfig.general.resistiveHeaterEfficiency.get();
	}

}
