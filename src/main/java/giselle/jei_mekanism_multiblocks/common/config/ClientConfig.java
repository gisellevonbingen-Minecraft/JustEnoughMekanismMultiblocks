package giselle.jei_mekanism_multiblocks.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;

public class ClientConfig
{
	private ModConfigSpec configSpec;

	public final BooleanValue dynamicTankVisible;
	public final BooleanValue evaporationPlantVisible;
	public final BooleanValue boilerVisible;
	public final BooleanValue spsVisible;
	public final BooleanValue matrixVisible;

	public final BooleanValue turbineVisible;
	public final BooleanValue fissionReactorVisible;
	public final BooleanValue fusionReactorVisible;

	public ClientConfig()
	{
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		builder.push("multiblocks");
		builder.push("mekanism");

		builder.comment("JEI 'Building [Dynamic Tank]' page configuration").push("dynamic_tank");
		this.dynamicTankVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Thermal Evaporation Plant]' page configuration").push("evaporation_plant");
		this.evaporationPlantVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Thermoelectric Boiler]' page configuration").push("boiler");
		this.boilerVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Supercritical Phase Shifter]' page configuration").push("sps");
		this.spsVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Induction Matrix]' page configuration").push("matrix");
		this.matrixVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.pop();
		builder.push("mekanismgenerators");

		builder.comment("JEI 'Building [Industrial Turbine]' page configuration").push("turbine");
		this.turbineVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Fission Reactor]' page configuration").push("fission_reactor");
		this.fissionReactorVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.comment("JEI 'Building [Fusion Reactor]' page configuration").push("fusion_reactor");
		this.fusionReactorVisible = builder.comment("Set page visibility").define("visible", true);
		builder.pop();

		builder.pop();
		builder.pop();
		this.configSpec = builder.build();
	}

	public ModConfigSpec getConfigSpec()
	{
		return this.configSpec;
	}

}
