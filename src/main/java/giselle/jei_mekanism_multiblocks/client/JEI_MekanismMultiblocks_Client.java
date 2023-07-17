package giselle.jei_mekanism_multiblocks.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class JEI_MekanismMultiblocks_Client
{
	public static void init()
	{
		IEventBus forge_bus = MinecraftForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks_Client.class);
	}

}
