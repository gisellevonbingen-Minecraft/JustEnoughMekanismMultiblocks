package giselle.jei_mekanism_multiblocks.client.jei.category;

import mekanism.api.heat.HeatAPI;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.core.Direction;

public class ResistiveHeaterCategory
{
	public static double getStableTemp(double ambientTemp, double heat, double inverseConduction)
	{
		double tempTarget = heat / TileEntityResistiveHeater.HEAT_CAPACITY;
		double totalInvConduction = inverseConduction + TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT;
		return (tempTarget * totalInvConduction) + ambientTemp;
	}

	public static double getEnvironmentLossHeat(double ambientTemp, double heaterTemp)
	{
		double totalInvConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + TileEntityResistiveHeater.INVERSE_INSULATION_COEFFICIENT + TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT;
		double tempToTransfer = (heaterTemp - ambientTemp) / totalInvConduction;
		return Direction.values().length * tempToTransfer * TileEntityResistiveHeater.HEAT_CAPACITY;
	}

	public static double getHeatTransferableEnergy(double ambientTemp, double heat, double inverseConduction)
	{
		double stableTemp = ResistiveHeaterCategory.getStableTemp(ambientTemp, heat, inverseConduction);
		double envLossHeat = ResistiveHeaterCategory.getEnvironmentLossHeat(ambientTemp, stableTemp);
		return ResistiveHeaterCategory.getEnergyForHeat(heat + envLossHeat);
	}

	public static double getEnergyForHeat(double heat)
	{
		return heat / MekanismConfig.general.resistiveHeaterEfficiency.get();
	}

}
