package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class MultiblockCategory<WIDGET extends MultiblockWidget> implements IRecipeCategory<WIDGET>
{
	private final RecipeType<WIDGET> type;
	private final IDrawable icon;
	private final IDrawable background;
	private final Component title;

	public MultiblockCategory(IGuiHelper helper, ResourceLocation name, Class<? extends WIDGET> clazz, Component multiblockName, ItemStack icon)
	{
		this(helper, name, clazz, multiblockName, helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon));
	}

	public MultiblockCategory(IGuiHelper helper, ResourceLocation name, Class<? extends WIDGET> clazz, Component multiblockName, IDrawable icon)
	{
		this.type = RecipeType.create(JEI_MekanismMultiblocks.MODID, "multiblock." + name.getNamespace() + "." + name.getPath(), clazz);
		this.icon = icon;
		this.background = helper.createBlankDrawable(180, 120);
		this.title = Component.translatable("text.jei_mekanism_multiblocks.recipe_category.title", multiblockName);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, WIDGET widget, IFocusGroup focuses)
	{
		IDrawable background = this.getBackground();
		widget.initialize();
		widget.setWidth(background.getWidth());
		widget.setHeight(background.getHeight());

		for (ItemStack cost : widget.getCosts())
		{
			this.addSlots(recipeLayout, cost);
		}

	}

	private void addSlots(IRecipeLayoutBuilder recipeLayout, ItemStack cost)
	{
		int maxStackSize = cost.getMaxStackSize();
		int count = cost.getCount();

		while (count > 0)
		{
			ItemStack item = cost.copy();
			item.setCount(Math.min(maxStackSize, count));
			count -= item.getCount();

			recipeLayout.addSlot(RecipeIngredientRole.INPUT, 9999, 9999).addItemStack(item);
		}

	}

	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{

	}

	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		RecipeType<WIDGET> type = this.getRecipeType();
		List<ItemStack> list = new ArrayList<>();
		this.getRecipeCatalystItemStacks(list::add);

		for (ItemStack itemStack : list)
		{
			registration.addRecipeCatalyst(itemStack, type);
		}

	}

	@Override
	public void draw(WIDGET widget, IRecipeSlotsView recipeSlotsView, GuiGraphics pGuiGraphics, double mouseX, double mouseY)
	{
		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();
		widget.render(pGuiGraphics, (int) mouseX, (int) mouseY, partialTicks);
	}

	@Override
	public List<Component> getTooltipStrings(WIDGET widget, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
	{
		if (widget.costsButton.isSelected())
		{
			CostWidget cost = widget.getCostUnderMouse(mouseX, mouseY);

			if (cost != null)
			{
				Minecraft minecraft = Minecraft.getInstance();
				List<Component> tooltip = new ArrayList<>();
				tooltip.addAll(Arrays.asList(cost.getHeadTooltip()));
				tooltip.addAll(Screen.getTooltipFromItem(minecraft, cost.getItemStack()));
				tooltip.addAll(Arrays.asList(cost.getTailTooltip()));
				return tooltip;
			}

		}

		return IRecipeCategory.super.getTooltipStrings(widget, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public boolean handleInput(WIDGET widget, double mouseX, double mouseY, Key input)
	{
		if (input.getType() == InputConstants.Type.MOUSE)
		{
			return widget.mouseClicked(mouseX, mouseY, input.getValue());
		}

		return IRecipeCategory.super.handleInput(widget, mouseX, mouseY, input);
	}

	public boolean handleScroll(WIDGET widget, double mouseX, double mouseY, double delta)
	{
		return widget.mouseScrolled(mouseX, mouseY, delta);
	}

	public boolean handleDrag(WIDGET widget, double mouseX, double mouseY, int mouseButton, double dragX, double dragY)
	{
		return widget.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
	}

	public boolean handleReleased(WIDGET widget, double mouseX, double mouseY, int mouseButton)
	{
		return widget.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public RecipeType<WIDGET> getRecipeType()
	{
		return this.type;
	}

	@Override
	public IDrawable getIcon()
	{
		return this.icon;
	}

	@Override
	public IDrawable getBackground()
	{
		return this.background;
	}

	@Override
	public Component getTitle()
	{
		return this.title;
	}

}
