package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
		this.title = new TranslatableComponent("text.jei_mekanism_multiblocks.recipe_category.title", multiblockName);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, WIDGET widget, IFocusGroup focuses)
	{
		IDrawable background = this.getBackground();
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
	public void draw(WIDGET widget, IRecipeSlotsView recipeSlotsView, PoseStack pPoseStack, double mouseX, double mouseY)
	{
		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();
		widget.render(pPoseStack, (int) mouseX, (int) mouseY, partialTicks);
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
	public ResourceLocation getUid()
	{
		return this.getRecipeType().getUid();
	}

	@Override
	public Class<? extends WIDGET> getRecipeClass()
	{
		return this.getRecipeType().getRecipeClass();
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
