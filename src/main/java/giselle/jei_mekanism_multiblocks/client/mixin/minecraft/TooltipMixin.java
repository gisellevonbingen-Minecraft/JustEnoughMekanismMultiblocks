package giselle.jei_mekanism_multiblocks.client.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import giselle.jei_mekanism_multiblocks.client.ITooltipAccessor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

@Mixin(value = Tooltip.class)
public abstract class TooltipMixin implements ITooltipAccessor
{
	@Shadow
	private Component message;

	@Shadow
	public Component narration;

	@Override
	@Unique
	public Component jei_mekanism_multiblocks$getMessage()
	{
		return this.message;
	}

	@Override
	@Unique
	public Component jei_mekanism_multiblocks$getNarration()
	{
		return this.narration;
	}

}
