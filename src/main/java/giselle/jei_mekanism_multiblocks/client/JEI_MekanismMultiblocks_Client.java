package giselle.jei_mekanism_multiblocks.client;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
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
	public static void onMouseScroll(GuiScreenEvent.MouseScrollEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleScroll(widget, mouseX, mouseY, e.getScrollDelta());
		});

	}

	@SubscribeEvent
	public static void onMouseDrag(GuiScreenEvent.MouseDragEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleDrag(widget, mouseX, mouseY, e.getMouseButton(), e.getDragX(), e.getDragY());
		});

	}

	@SubscribeEvent
	public static void onMouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre e)
	{
		foreachMouseInput(e, (category, widget, mouseX, mouseY) ->
		{
			category.handleReleased(widget, mouseX, mouseY, e.getButton());
		});

	}

	public static void foreachMouseInput(MouseInputEvent e, MouseInputHandler handler)
	{
		Screen screen = e.getGui();
		double mouseX = e.getMouseX();
		double mouseY = e.getMouseY();

		if (screen.isMouseOver(mouseX, mouseY))
		{
			getRecipeLayouts(screen).forEach(pair ->
			{
				RecipeLayout<MultiblockWidget> recipeLayout = pair.getRecipeLayout();

				if (recipeLayout.isMouseOver(mouseX, mouseY))
				{
					int posX = recipeLayout.getPosX();
					int posY = recipeLayout.getPosY();
					double recipeMouseX = mouseX - posX;
					double recipeMouseY = mouseY - posY;
					handler.handle(pair.getRecipeCategory(), recipeLayout.getRecipe(), recipeMouseX, recipeMouseY);
				}

			});

		}

	}

	@SuppressWarnings("unchecked")
	public static List<RecipeLayoutWithCategory<MultiblockCategory<MultiblockWidget>, MultiblockWidget>> getRecipeLayouts(Screen screen)
	{
		if (screen instanceof IRecipeLayoutHolder)
		{
			return ((IRecipeLayoutHolder) screen).jei_mekanism_multiblocks$getRecipeLayouts().stream()//
					.filter(recipeLayout -> recipeLayout.getRecipeCategory() instanceof MultiblockCategory<?>)//
					.map(recipeLayout -> new RecipeLayoutWithCategory<>((RecipeLayout<MultiblockWidget>) recipeLayout, (MultiblockCategory<MultiblockWidget>) recipeLayout.getRecipeCategory())).collect(Collectors.toList());
		}
		else
		{
			return Collections.emptyList();
		}

	}

}
