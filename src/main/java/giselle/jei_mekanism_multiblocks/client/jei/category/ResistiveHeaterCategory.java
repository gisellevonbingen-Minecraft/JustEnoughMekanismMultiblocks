package giselle.jei_mekanism_multiblocks.client.jei.category;

import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.Direction;

public class ResistiveHeaterCategory
{
	public static final double HEAT_CAPACITY = 100.0D;
	public static final double INVERSE_CONDUCTION_COEFFICIENT = 5.0D;
	public static final double INVERSE_INSULATION_COEFFICIENT = 100.0D;

	public static double getStableTemp(double ambientTemp, double heat, double inverseConduction)
	{
		double tempTarget = heat / HEAT_CAPACITY;
		double totalInvConduction = inverseConduction + INVERSE_CONDUCTION_COEFFICIENT;
		return (tempTarget * totalInvConduction) + ambientTemp;
	}

	public static double getEnvironmentLossHeat(double ambientTemp, double heaterTemp)
	{
		double totalInvConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + INVERSE_INSULATION_COEFFICIENT + INVERSE_CONDUCTION_COEFFICIENT;
		double tempToTransfer = (heaterTemp - ambientTemp) / totalInvConduction;
		return Direction.values().length * tempToTransfer * HEAT_CAPACITY;
	}

	public static FloatingLong getHeatTransferableEnergy(double ambientTemp, double heat, double inverseConduction)
	{
		double stableTemp = ResistiveHeaterCategory.getStableTemp(ambientTemp, heat, inverseConduction);
		double envLossHeat = ResistiveHeaterCategory.getEnvironmentLossHeat(ambientTemp, stableTemp);
		return FloatingLong.create(ResistiveHeaterCategory.getEnergyForHeat(heat + envLossHeat));
	}

	public static double getEnergyForHeat(double heat)
	{
		return heat / MekanismConfig.general.resistiveHeaterEfficiency.get();
	}

}
