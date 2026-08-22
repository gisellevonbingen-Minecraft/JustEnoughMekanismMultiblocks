package giselle.jei_mekanism_multiblocks.client.jei.category;

import mekanism.api.heat.HeatAPI;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.core.Direction;

public class ResistiveHeaterCategory
{
	public static double getStableTemp(double heat)
	{
		double tempTarget = heat / TileEntityResistiveHeater.HEAT_CAPACITY;
		double inverseConduction = HeatAPI.DEFAULT_INVERSE_CONDUCTION + TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT;

		return (tempTarget * inverseConduction) + HeatAPI.AMBIENT_TEMP;
	}

	public static double getEnvironmentLossHeat(double temp)
	{
		double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + TileEntityResistiveHeater.INVERSE_INSULATION_COEFFICIENT + TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT;
		double tempToTransfer = (temp - HeatAPI.AMBIENT_TEMP) / invConduction;
		return Direction.values().length * tempToTransfer * TileEntityResistiveHeater.HEAT_CAPACITY;
	}

	public static double getHeatTransferableEnergy(double heat)
	{
		double stableTemp = ResistiveHeaterCategory.getStableTemp(heat);
		double envLossHeat = ResistiveHeaterCategory.getEnvironmentLossHeat(stableTemp);
		return ResistiveHeaterCategory.getEnergyForHeat(heat + envLossHeat);
	}

	public static double getEnergyForHeat(double heat)
	{
		return heat / MekanismConfig.general.resistiveHeaterEfficiency.get();
	}

}
