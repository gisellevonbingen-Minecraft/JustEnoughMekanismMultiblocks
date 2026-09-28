package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;

public class CostList extends ListLineWidget
{
	public CostList(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	public void updateCosts(List<CostWidget> widgets)
	{
		this.getItems().clearChildren();

		for (CostWidget widget : widgets)
		{
			if (widget.getItemStack().isEmpty())
			{
				continue;
			}

			this.getItems().addChild(widget);
		}

	}

	public List<ItemStack> getCosts()
	{
		List<ItemStack> costs = new ArrayList<>();

		for (Widget widget : this.getItems().getChildren())
		{
			if (widget instanceof CostWidget)
			{
				costs.add(((CostWidget) widget).getItemStack());
			}

		}

		return costs;
	}

}
