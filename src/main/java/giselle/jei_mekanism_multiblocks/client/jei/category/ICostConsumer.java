package giselle.jei_mekanism_multiblocks.client.jei.category;

import giselle.jei_mekanism_multiblocks.client.jei.CostWidget;
import net.minecraft.world.item.ItemStack;

public interface ICostConsumer
{
	public default CostWidget accept(ItemStack itemStack)
	{
		return this.accept(new CostWidget(0, 0, 0, 0, itemStack));
	}

	CostWidget accept(CostWidget widget);
}
