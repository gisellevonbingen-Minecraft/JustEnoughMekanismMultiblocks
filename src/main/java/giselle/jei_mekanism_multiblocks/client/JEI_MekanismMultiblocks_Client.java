package giselle.jei_mekanism_multiblocks.client;

import java.util.Collections;
import java.util.List;

import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class JEI_MekanismMultiblocks_Client
{
	public static void init()
	{
		IEventBus forge_bus = NeoForge.EVENT_BUS;
		forge_bus.register(JEI_MekanismMultiblocks_Client.class);
	}

	@FunctionalInterface
	public static interface MouseInputHandler
	{
		void handle(MultiblockCategory<MultiblockWidget> category, MultiblockWidget widget, double mouseX, double mouseY);
	}

	@SubscribeEvent
	public static void onMouseScroll(ScreenEvent.MouseScrolled.Pre e)
	{
		foreachMouseInput(e, e.getMouseX(), e.getMouseY(), (category, widget, mouseX, mouseY) ->
		{
			category.handleScroll(widget, mouseX, mouseY, e.getScrollDeltaX(), e.getScrollDeltaY());
		});

	}

	@SubscribeEvent
	public static void onMouseDrag(ScreenEvent.MouseDragged.Pre e)
	{
		foreachMouseInput(e, e.getMouseX(), e.getMouseY(), (category, widget, mouseX, mouseY) ->
		{
			category.handleDrag(widget, mouseX, mouseY, e.getMouseButton(), e.getDragX(), e.getDragY());
		});

	}

	@SubscribeEvent
	public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre e)
	{
		foreachMouseInput(e, e.getMouseX(), e.getMouseY(), (category, widget, mouseX, mouseY) ->
		{
			category.handleReleased(widget, mouseX, mouseY, e.getButton());
		});

	}

	public static void foreachMouseInput(ScreenEvent e, double mouseX, double mouseY, MouseInputHandler handler)
	{
		Screen screen = e.getScreen();

		if (screen.isMouseOver(mouseX, mouseY))
		{
			getRecipeLayouts(screen).forEach(pair ->
			{
				IRecipeLayoutDrawable<MultiblockWidget> recipeLayout = pair.getRecipeLayout();

				if (recipeLayout.isMouseOver(mouseX, mouseY))
				{
					Rect2i rect = recipeLayout.getRect();
					double recipeMouseX = mouseX - rect.getX();
					double recipeMouseY = mouseY - rect.getY();
					handler.handle(pair.getRecipeCategory(), recipeLayout.getRecipe(), recipeMouseX, recipeMouseY);
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
					.filter(recipeLayout -> recipeLayout.getRecipeCategory() instanceof MultiblockCategory<?>)//
					.map(recipeLayout -> new RecipeLayoutWithCategory<>((IRecipeLayoutDrawable<MultiblockWidget>) recipeLayout, (MultiblockCategory<MultiblockWidget>) recipeLayout.getRecipeCategory())).toList();
		}
		else
		{
			return Collections.emptyList();
		}

	}

}
