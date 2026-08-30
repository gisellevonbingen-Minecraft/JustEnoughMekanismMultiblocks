package giselle.jei_mekanism_multiblocks.client;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class JEI_MekanismMultiblocks_Client
{
	private static int SAVE_TIMER = 0;

	public static boolean MOUSE_PRESSED;
	public static boolean MOUSE_RELEASED;
	public static boolean MOUSE_DRAGGED;
	public static double MOUSE_SCROLLED;

	public static void init()
	{
		IEventBus forge_bus = MinecraftForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks_Client.class);

		SavedData.load();
	}

	public static void markNeedSave()
	{
		SAVE_TIMER = 20;
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent e)
	{
		if (e.phase == Phase.END && SAVE_TIMER > 0)
		{
			SAVE_TIMER--;

			if (SAVE_TIMER == 0)
			{
				SavedData.save();
			}

		}

	}

	@SubscribeEvent
	public static void onScreenRenderPre(ScreenEvent.Render.Post e)
	{
		MOUSE_PRESSED = false;
		MOUSE_RELEASED = false;
		MOUSE_DRAGGED = false;
		MOUSE_SCROLLED = 0.0D;
	}

	@SubscribeEvent
	public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre e)
	{
		if (e.getButton() == 0)
		{
			MOUSE_PRESSED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseButtonReleased(ScreenEvent.MouseButtonReleased.Pre e)
	{
		if (e.getButton() == 0)
		{
			MOUSE_RELEASED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseDragged(ScreenEvent.MouseDragged.Pre e)
	{
		if (e.getMouseButton() == 0)
		{
			MOUSE_DRAGGED = true;
		}

	}

	@SubscribeEvent
	public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre e)
	{
		MOUSE_SCROLLED = e.getScrollDelta();
	}

}
