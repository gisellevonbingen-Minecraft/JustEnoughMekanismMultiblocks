package giselle.jei_mekanism_multiblocks.common.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class JEI_MekanismMultiblocks_Config
{
	public static final ClientConfig CLIENT = new ClientConfig();

	private JEI_MekanismMultiblocks_Config()
	{

	}

	public static void registerConfigs(ModLoadingContext modLoadingContext)
	{
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, CLIENT.getConfigSpec());
	}

}
