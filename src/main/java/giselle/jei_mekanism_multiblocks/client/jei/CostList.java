package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

public class CostList extends ListLineWidget
{
	public CostList(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	public Optional<Object> getIngredientUnderMouse(double pMouseX, double pMouseY)
	{
		for (AbstractWidget widget : this.getChildren())
		{
			if (widget instanceof CostWidget cost && widget.isMouseOver(pMouseX, pMouseY))
			{
				return Optional.ofNullable(cost.getItemStack());
			}

		}

		return Optional.empty();
	}

	public void updateCosts(List<CostWidget> widgets)
	{
		this.clearChildren();

		for (CostWidget widget : widgets)
		{
			if (widget.getItemStack().isEmpty())
			{
				continue;
			}

			this.addChild(widget);
		}

	}

	public List<ItemStack> getCosts()
	{
		List<ItemStack> costs = new ArrayList<>();

		for (AbstractWidget widget : this.getChildren())
		{
			if (widget instanceof CostWidget cost)
			{
				costs.add(cost.getItemStack());
			}

		}

		return costs;
	}

}
