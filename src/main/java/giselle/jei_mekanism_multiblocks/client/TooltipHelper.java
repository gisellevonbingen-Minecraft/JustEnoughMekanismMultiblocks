package giselle.jei_mekanism_multiblocks.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import giselle.jei_mekanism_multiblocks.client.mixin.minecraft.TooltipAccessor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TooltipHelper
{
	public static Tooltip mergetMessage(Tooltip pTooltip, Collection<Component> additions)
	{
		List<Component> elements = new ArrayList<>();
		Component narration = null;

		if (pTooltip != null)
		{
			TooltipAccessor accessor = (TooltipAccessor) pTooltip;
			elements.add(accessor.getMessage());
			narration = accessor.getNarration();
		}

		elements.addAll(additions);

		return create(elements, narration);
	}

	public static Tooltip createMessageOnly(Component... elements)
	{
		return create(Arrays.asList(elements));
	}

	public static Tooltip create(List<Component> elements)
	{
		Component component = merge(elements);
		return Tooltip.create(component, component);
	}

	public static Tooltip create(List<Component> elements, Component narration)
	{
		Component component = merge(elements);
		return Tooltip.create(component, narration);
	}

	public static Component merge(List<Component> elements)
	{
		MutableComponent component = Component.empty();

		for (int i = 0; i < elements.size(); i++)
		{
			if (i > 0)
			{
				component = component.append("\n");
			}

			component = component.append(elements.get(i));
		}

		return component;
	}

	private TooltipHelper()
	{

	}

}
