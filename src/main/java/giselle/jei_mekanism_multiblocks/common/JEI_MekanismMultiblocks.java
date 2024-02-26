package giselle.jei_mekanism_multiblocks.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import giselle.jei_mekanism_multiblocks.client.JEI_MekanismMultiblocks_Client;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(JEI_MekanismMultiblocks.MODID)
public class JEI_MekanismMultiblocks
{
	public static final String MODID = "jei_mekanism_multiblocks";
	public static final Logger LOGGER = LogManager.getLogger();

	public static boolean MekanismGeneratorsLoaded = false;

	public JEI_MekanismMultiblocks()
	{
		JEI_MekanismMultiblocks_Config.registerConfigs(ModLoadingContext.get());

		if (FMLEnvironment.dist.isClient())
		{
			JEI_MekanismMultiblocks_Client.init();
		}

		IEventBus fml_bus = ModLoadingContext.get().getActiveContainer().getEventBus();
		fml_bus.addListener(JEI_MekanismMultiblocks::onCommonSetup);
	}

	private static void onCommonSetup(FMLCommonSetupEvent e)
	{
		ModList modList = ModList.get();
		MekanismGeneratorsLoaded = modList.isLoaded("mekanismgenerators");
	}

	public static ResourceLocation rl(String path)
	{
		return new ResourceLocation(MODID, path);
	}

}
