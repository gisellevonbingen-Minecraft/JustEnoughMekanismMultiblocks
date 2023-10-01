package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

public class CostList extends ListLineWidget
{
	public CostList(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	public CostWidget getCostUnderMouse(double pMouseX, double pMouseY)
	{
		for (AbstractWidget widget : this.getChildren())
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);

			if (widget instanceof CostWidget cost && widget.isMouseOver(childMouseX, childMouseY))
			{
				return cost;
			}

		}

		return null;
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
