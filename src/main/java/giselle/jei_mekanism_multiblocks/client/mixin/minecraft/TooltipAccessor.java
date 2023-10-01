package giselle.jei_mekanism_multiblocks.client.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

@Mixin(value = Tooltip.class)
public interface TooltipAccessor
{
	@Accessor
	public Component getMessage();

	@Accessor
	public Component getNarration();
}
