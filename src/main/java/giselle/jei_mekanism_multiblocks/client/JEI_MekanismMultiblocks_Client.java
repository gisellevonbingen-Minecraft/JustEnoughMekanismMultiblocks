package giselle.jei_mekanism_multiblocks.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class JEI_MekanismMultiblocks_Client
{
	public static boolean PRESSED;
	public static boolean RELEASED;
	public static boolean DRAGGED;
	public static double SCROLLED;

	public static void init()
	{
		IEventBus forge_bus = NeoForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks_Client.class);
	}

	@SubscribeEvent
	public static void onScreenRenderPre(ScreenEvent.Render.Post e)
	{
		PRESSED = false;
		RELEASED = false;
		DRAGGED = false;
		SCROLLED = 0.0D;
	}

	@SubscribeEvent
	public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre e)
	{
		if (e.getButton() == 0)
		{
			PRESSED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseButtonReleased(ScreenEvent.MouseButtonReleased.Pre e)
	{
		if (e.getButton() == 0)
		{
			RELEASED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseDragged(ScreenEvent.MouseDragged.Pre e)
	{
		if (e.getMouseButton() == 0)
		{
			DRAGGED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre e)
	{
		SCROLLED = e.getScrollDeltaY();
	}

}
