package giselle.jei_mekanism_multiblocks.client.jei;

import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public class WidgetInputHandler implements IJeiGuiEventListener
{
	private final MultiblockWidget widget;
	private final ScreenRectangle area;

	public WidgetInputHandler(MultiblockWidget widget, ScreenRectangle area)
	{
		this.widget = widget;
		this.area = area;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY)
	{
		this.widget.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		return this.widget.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		return this.widget.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		return this.widget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
	{
		return this.widget.mouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers)
	{
		return this.widget.keyPressed(keyCode, scanCode, modifiers);
	}

	public MultiblockWidget getWidget()
	{
		return this.widget;
	}

	@Override
	public ScreenRectangle getArea()
	{
		return this.area;
	}

}
