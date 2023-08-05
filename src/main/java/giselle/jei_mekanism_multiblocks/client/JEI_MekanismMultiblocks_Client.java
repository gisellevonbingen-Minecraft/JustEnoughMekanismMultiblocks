package giselle.jei_mekanism_multiblocks.client;

import java.util.Collections;
import java.util.List;

import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class JEI_MekanismMultiblocks_Client
{
	public static void init()
	{
		IEventBus forge_bus = MinecraftForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks_Client.class);
	}

	@FunctionalInterface
	public static interface MouseInputHandler
	{
		void handle(MultiblockCategory<MultiblockWidget> category, MultiblockWidget widget, double mouseX, double mouseY);
	}

	@SubscribeEvent
	public static void onMouseScroll(ScreenEvent.MouseScrollEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleScroll(widget, mouseX, mouseY, e.getScrollDelta());
		});

	}

	@SubscribeEvent
	public static void onMouseDrag(ScreenEvent.MouseDragEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleDrag(widget, mouseX, mouseY, e.getMouseButton(), e.getDragX(), e.getDragY());
		});

	}

	@SubscribeEvent
	public static void onMouseReleased(ScreenEvent.MouseReleasedEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleReleased(widget, mouseX, mouseY, e.getButton());
		});

	}

	public static void foreachMouseInput(MouseInputEvent e, MouseInputHandler handler)
	{
		Screen screen = e.getScreen();
		double mouseX = e.getMouseX();
		double mouseY = e.getMouseY();

		if (screen.isMouseOver(mouseX, mouseY))
		{
			getRecipeLayouts(screen).forEach(pair ->
			{
				IRecipeLayout<MultiblockWidget> recipeLayout = pair.getRecipeLayout();

				if (recipeLayout.jei_mekanism_multiblocks$isMouseOver(mouseX, mouseY))
				{
					int posX = recipeLayout.jei_mekanism_multiblocks$getPosX();
					int posY = recipeLayout.jei_mekanism_multiblocks$getPosY();
					double recipeMouseX = mouseX - posX;
					double recipeMouseY = mouseY - posY;
					handler.handle(pair.getRecipeCategory(), recipeLayout.jei_mekanism_multiblocks$getRecipe(), recipeMouseX, recipeMouseY);
				}

			});

		}

	}

	@SuppressWarnings("unchecked")
	public static List<RecipeLayoutWithCategory<MultiblockCategory<MultiblockWidget>, MultiblockWidget>> getRecipeLayouts(Screen screen)
	{
		if (screen instanceof IRecipeLayoutHolder holder)
		{
			return holder.jei_mekanism_multiblocks$getRecipeLayouts().stream()//
					.filter(recipeLayout -> recipeLayout.jei_mekanism_multiblocks$getRecipeCategory() instanceof MultiblockCategory<?>)//
					.map(recipeLayout -> new RecipeLayoutWithCategory<>((IRecipeLayout<MultiblockWidget>) recipeLayout, (MultiblockCategory<MultiblockWidget>) recipeLayout.jei_mekanism_multiblocks$getRecipeCategory())).toList();
		}
		else
		{
			return Collections.emptyList();
		}

	}

}
