package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import giselle.jei_mekanism_multiblocks.client.gui.ListWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;

public class CostList extends ListWidget
{
	public CostList(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	public Optional<Object> getIngredientUnderMouse(double pMouseX, double pMouseY)
	{
		for (Widget widget : this.getChildren())
		{
			if (widget instanceof CostWidget && widget.isMouseOver(pMouseX, pMouseY))
			{
				return Optional.ofNullable(((CostWidget) widget).getItemStack());
			}

		}

		return Optional.empty();
	}

	public void updateCosts(Collection<ItemStack> costs)
	{
		this.clearChildren();

		for (ItemStack cost : costs)
		{
			if (cost.isEmpty())
			{
				continue;
			}

			this.addChild(new CostWidget(0, 0, 0, 0, cost));
		}

	}

	public List<ItemStack> getCosts()
	{
		List<ItemStack> costs = new ArrayList<>();

		for (Widget widget : this.getChildren())
		{
			if (widget instanceof CostWidget)
			{
				costs.add(((CostWidget) widget).getItemStack());
			}

		}

		return costs;
	}

}
