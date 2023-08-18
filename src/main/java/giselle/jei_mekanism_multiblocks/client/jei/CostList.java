package giselle.jei_mekanism_multiblocks.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;

public class CostList extends ListLineWidget
{
	public CostList(int pX, int pY, int pWidth, int pHeight, int itemHeight)
	{
		super(pX, pY, pWidth, pHeight, itemHeight);
	}

	public Optional<Object> getIngredientUnderMouse(double pMouseX, double pMouseY)
	{
		for (Widget widget : this.getChildren())
		{
			double childMouseX = this.toChildX(pMouseX);
			double childMouseY = this.toChildY(pMouseY);
			
			if (widget instanceof CostWidget && widget.isMouseOver(childMouseX, childMouseY))
			{
				return Optional.ofNullable(((CostWidget) widget).getItemStack());
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
