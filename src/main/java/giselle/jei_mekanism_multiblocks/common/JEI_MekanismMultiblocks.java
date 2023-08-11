package giselle.jei_mekanism_multiblocks.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import giselle.jei_mekanism_multiblocks.client.JEI_MekanismMultiblocks_Client;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JEI_MekanismMultiblocks.MODID)
public class JEI_MekanismMultiblocks
{
	public static final String MODID = "jei_mekanism_multiblocks";
	public static final Logger LOGGER = LogManager.getLogger();

	public static boolean MekanismGeneratorsLoaded = false;

	public JEI_MekanismMultiblocks()
	{
		JEI_MekanismMultiblocks_Config.registerConfigs(ModLoadingContext.get());
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> JEI_MekanismMultiblocks_Client::init);

		IEventBus fml_bus = FMLJavaModLoadingContext.get().getModEventBus();
		fml_bus.addListener(JEI_MekanismMultiblocks::onCommonSetup);

		IEventBus forge_bus = MinecraftForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks.class);
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
