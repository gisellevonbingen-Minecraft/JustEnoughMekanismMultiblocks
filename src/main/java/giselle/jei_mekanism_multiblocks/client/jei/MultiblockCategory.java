package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class MultiblockCategory<WIDGET extends MultiblockWidget> implements IRecipeCategory<WIDGET>
{
	private final ResourceLocation id;
	private final IDrawable icon;
	private final IDrawable background;
	private final ITextComponent title;

	public MultiblockCategory(IGuiHelper helper, String name, ITextComponent multiblockName, ItemStack icon)
	{
		this(helper, name, multiblockName, helper.createDrawableIngredient(icon));
	}

	public MultiblockCategory(IGuiHelper helper, String name, ITextComponent multiblockName, IDrawable icon)
	{
		this.id = JEI_MekanismMultiblocks.rl(name);
		this.icon = icon;
		this.background = helper.createBlankDrawable(180, 120);
		this.title = new TranslationTextComponent("text.jei_mekanism_multiblocks.recipe_category.title", multiblockName);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, WIDGET widget, IIngredients ingredients)
	{
		IDrawable background = this.getBackground();
		widget.setWidth(background.getWidth());
		widget.setHeight(background.getHeight());

		IGuiItemStackGroup items = recipeLayout.getItemStacks();
		int slotIndex = 0;

		for (ItemStack cost : widget.getCosts())
		{
			slotIndex = this.addSlots(items, slotIndex, cost);
		}

	}

	private int addSlots(IGuiItemStackGroup items, int slotIndex, ItemStack cost)
	{
		int maxStackSize = cost.getMaxStackSize();
		int count = cost.getCount();

		for (; count > 0; slotIndex++)
		{
			ItemStack item = cost.copy();
			item.setCount(Math.min(maxStackSize, count));
			count -= item.getCount();

			items.init(slotIndex, true, -9999, -9999);
			items.set(slotIndex, item);
		}

		return slotIndex;
	}

	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{

	}

	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		ResourceLocation uid = this.getUid();
		List<ItemStack> list = new ArrayList<>();
		this.getRecipeCatalystItemStacks(list::add);

		for (ItemStack itemStack : list)
		{
			registration.addRecipeCatalyst(itemStack, uid);
		}

	}

	@Override
	public void draw(WIDGET widget, MatrixStack matrix, double mouseX, double mouseY)
	{
		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();
		widget.render(matrix, (int) mouseX, (int) mouseY, partialTicks);
	}

	@Override
	public boolean handleClick(WIDGET widget, double mouseX, double mouseY, int mouseButton)
	{
		return widget.mouseClicked(mouseX, mouseY, mouseButton);
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
	public ResourceLocation getUid()
	{
		return this.id;
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
	public ITextComponent getTitleAsTextComponent()
	{
		return this.title;
	}

	@Override
	public String getTitle()
	{
		return this.title.getString();
	}

}
