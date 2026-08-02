package giselle.jei_mekanism_multiblocks.client.gui;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import giselle.jei_mekanism_multiblocks.client.jei.ListLineWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.FissionReactorCategory.FissionReactorCategoryWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.FissionReactorCategory.FissionReactorCategoryWidget.Layout;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class FissionLayoutScreen extends Screen
{
	private static final ResourceLocation TEXTURE = JEI_MekanismMultiblocks.rl("textures/gui/fission_layout.png");
	private static final Int2ObjectMap<Component> PILLAR_COMPONENT_CACHE = new Int2ObjectOpenHashMap<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Component COMPONENT_RESET = Component.translatable("text.jei_mekanism_multiblocks.reset");
	private static final Component COMPONENT_CLEAR = Component.translatable("text.jei_mekanism_multiblocks.clear");
	private static final Component COMPONENT_SAVE = Component.translatable("text.jei_mekanism_multiblocks.save");
	private static final Component COMPONENT_CHECK = Component.literal("✔");
	private static final Component COMPONENT_ERROR = Component.literal("❌");
	private static final Component COMPONENT_JSON_SYNTAX_ERROR = Component.translatable("text.jei_mekanism_multiblocks.json_syntax_error");
	private static final Component COMPONENT_IMPORT = Component.translatable("text.jei_mekanism_multiblocks.import");
	private static final Component COMPONENT_IMPORT_FROM_CLIPBOARD = Component.translatable("text.jei_mekanism_multiblocks.import_from_clipboard");
	private static final Component COMPONENT_IMPORTED_SUCCESSFULLY = Component.translatable("text.jei_mekanism_multiblocks.imported_successfully");
	private static final Component COMPONENT_EXPORT = Component.translatable("text.jei_mekanism_multiblocks.export");
	private static final Component COMPONENT_EXPORT_TO_CLIPBOARD = Component.translatable("text.jei_mekanism_multiblocks.export_to_clipboard");
	private static final Component COMPONENT_EXPORTED_SUCCESSFULLY = Component.translatable("text.jei_mekanism_multiblocks.exported_successfully");
	private static final Component COMPONENT_MAX_BURN_RATE = Component.translatable("text.jei_mekanism_multiblocks.result.max_burn_rate");
	private static final Component COMPONENT_BOIL_EFFICIENCY = Component.translatable("text.jei_mekanism_multiblocks.result.boil_efficiency");

	private static final int GRID_X = 106;
	private static final int GRID_Y = 3;
	private static final int CELL_WIDTH = 13;
	private static final int CELL_HEIGHT = 13;

	private final Object2IntMap<Component> pillarTextWidthCache = new Object2IntOpenHashMap<>();
	private final FissionReactorCategoryWidget widget;
	private ButtonWidget importButton;
	private ButtonWidget exportButton;
	private IntSliderWithButtons widthSlider;
	private IntSliderWithButtons lengthSlider;
	private IntSliderWithButtons heightSlider;
	private LongSliderWithButtons burnRateSlider;
	private ButtonWidget resetButton;
	private ButtonWidget clearButton;
	private ButtonWidget saveButton;
	private ListLineWidget resultsList;
	private Result importResult;
	private Result exportResult;

	private boolean keepBurnRate = true;
	private boolean layoutDirty;
	private boolean resultDirty;
	private FissionReactorCategoryWidget.Layout layout;
	private long burnRate;
	private FissionReactorCategoryWidget simulation;

	private boolean dragging;
	private int prevX;
	private int prevZ;
	private int dragPillar;

	public FissionLayoutScreen(Component title, FissionReactorCategoryWidget widget)
	{
		super(title);

		this.widget = widget;

		this.layoutDirty = true;
		this.layout = widget.getAdvancedLayout();
		this.burnRate = widget.getBurnRate();
		this.simulation = new FissionReactorCategoryWidget();
	}

	@Override
	protected void init()
	{
		super.init();

		int widgetX = 3;
		int widgetY = 3;
		int widgetWidth = 100;
		int widgetHeight = 10;
		int widgetOffset = 3;
		Layout layout = this.layout;

		this.importButton = new ButtonWidget(widgetX, widgetY, widgetWidth / 2, 15, COMPONENT_IMPORT);
		this.importButton.setTooltip(Tooltip.create(COMPONENT_IMPORT_FROM_CLIPBOARD));
		this.importButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.importButton);

		this.exportButton = new ButtonWidget(widgetX + this.importButton.getWidth(), widgetY, widgetWidth / 2, 15, COMPONENT_EXPORT);
		this.exportButton.setTooltip(Tooltip.create(COMPONENT_EXPORT_TO_CLIPBOARD));
		this.exportButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.exportButton);
		widgetY += this.exportButton.getHeight() + widgetOffset;

		this.widthSlider = new IntSliderWithButtons(widgetX, widgetY, widgetWidth, widgetHeight, "text.jei_mekanism_multiblocks.specs.width", layout.getWidth(), this.simulation.getDimensionWidthMin(), this.simulation.getDimensionWidthMax());
		this.widthSlider.getSlider().addValueChangeHanlder(this::onDimensionWidthChanged);
		this.addRenderableWidget(this.widthSlider);
		widgetY += this.widthSlider.getHeight() + widgetOffset;

		this.lengthSlider = new IntSliderWithButtons(widgetX, widgetY, widgetWidth, widgetHeight, "text.jei_mekanism_multiblocks.specs.length", layout.getLength(), this.simulation.getDimensionLengthMin(), this.simulation.getDimensionLengthMax());
		this.lengthSlider.getSlider().addValueChangeHanlder(this::onDimensionLengthChanged);
		this.addRenderableWidget(this.lengthSlider);
		widgetY += this.lengthSlider.getHeight() + widgetOffset;

		this.heightSlider = new IntSliderWithButtons(widgetX, widgetY, widgetWidth, widgetHeight, "text.jei_mekanism_multiblocks.specs.height", layout.getHeight(), this.simulation.getDimensionHeightMin(), this.simulation.getDimensionHeightMax());
		this.heightSlider.getSlider().addValueChangeHanlder(this::onDimensionHeightChanged);
		this.addRenderableWidget(this.heightSlider);
		widgetY += this.heightSlider.getHeight() + widgetOffset;

		this.burnRateSlider = new LongSliderWithButtons(widgetX, widgetY, widgetWidth, widgetHeight, "text.jei_mekanism_multiblocks.specs.burn_rate", this.burnRate, 0, this.simulation.getMaxBurnRate());
		this.burnRateSlider.getSlider().addValueChangeHanlder(this::onBurnRateChanged);
		this.addRenderableWidget(this.burnRateSlider);
		widgetY += this.burnRateSlider.getHeight() + widgetOffset;

		this.resetButton = new ButtonWidget(widgetX, widgetY, widgetWidth, 15, COMPONENT_RESET);
		this.resetButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.resetButton);
		widgetY += this.resetButton.getHeight() + widgetOffset;

		this.clearButton = new ButtonWidget(widgetX, widgetY, widgetWidth, 15, COMPONENT_CLEAR);
		this.clearButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.clearButton);
		widgetY += this.clearButton.getHeight() + widgetOffset;

		this.resultsList = new ListLineWidget(widgetX, widgetY, widgetWidth, this.height - widgetY - 15 - widgetOffset, 20);
		this.resultsList.drawBG = true;
		this.resultsList.bgColor = 0xFFA0A0A0;
		this.resultsList.setItemsPadding(2);
		this.resultsList.setItemOffset(1);
		this.addRenderableWidget(this.resultsList);
		widgetY += this.resultsList.getHeight() + widgetOffset;

		this.saveButton = new ButtonWidget(widgetX, widgetY, widgetWidth, 15, COMPONENT_SAVE);
		this.saveButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.saveButton);
		widgetY += this.saveButton.getHeight() + widgetOffset;

		this.layoutDirty = true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		Layout layout = this.layout;

		for (int z = 0; z < layout.getLength(); z++)
		{
			boolean b1 = z == 0 || z == layout.getLength() - 1;

			for (int x = 0; x < layout.getWidth(); x++)
			{
				boolean outer = b1 || x == 0 || x == layout.getWidth() - 1;
				int coordZ = GRID_Y + CELL_HEIGHT * z;
				int coordX = GRID_X + CELL_WIDTH * x;

				int u = 0;
				int v = 0;

				if (!outer)
				{
					u += CELL_WIDTH;

					if (coordX <= mouseX && mouseX < coordX + CELL_WIDTH && coordZ <= mouseY && mouseY < coordZ + CELL_HEIGHT)
					{
						u += CELL_WIDTH;
					}

				}

				guiGraphics.blit(TEXTURE, coordX, coordZ, u, v, CELL_WIDTH, CELL_HEIGHT);

				if (!outer)
				{
					int pillar = layout.getPillar(x, z);

					if (pillar > 0)
					{
						Component text = PILLAR_COMPONENT_CACHE.computeIfAbsent(pillar, s -> Component.literal(String.valueOf(s)));
						int textWidth = this.pillarTextWidthCache.computeIfAbsent(text, s -> this.font.width((Component) s));
						int textHeight = this.font.lineHeight;
						guiGraphics.drawString(this.font, text, coordX + (CELL_WIDTH - textWidth) / 2, coordZ + (CELL_HEIGHT - textHeight), 0xFFFFFFFF, false);
					}

				}

			}

		}

		if (this.layoutDirty)
		{
			long burnRateFirst = this.burnRate;
			this.updateLayout();

			LongSliderWidget burnRateSlider = this.burnRateSlider.getSlider();
			burnRateSlider.setValue(this.keepBurnRate ? burnRateFirst : burnRateSlider.getMaxValue());
			this.keepBurnRate = false;
		}

		if (this.resultDirty)
		{
			this.resultDirty = false;
			this.resultsList.clearChildren();
			this.resultsList.addChild(new ResultWidget(COMPONENT_BOIL_EFFICIENCY, Component.literal(String.valueOf(Math.round(this.simulation.getBoilEfficiency() * 1000.0D) / 1000.0D))));
			this.resultsList.addChild(new ResultWidget(COMPONENT_MAX_BURN_RATE, VolumeTextHelper.formatMBt(this.burnRateSlider.getSlider().getMaxValue())));
			this.simulation.createStableTempWidgets(this.resultsList::addChild);

			long coolantCapacity = this.simulation.getCooledCoolantCapacity();
			long heatedCoolantCapacity = this.simulation.getHeatedCoolantCapacity();
			long fuelCapacity = this.simulation.getFuelCapacity();
			this.resultsList.addChild(new ResultWidget(GeneratorsLang.FISSION_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(coolantCapacity)));
			this.resultsList.addChild(new ResultWidget(GeneratorsLang.FISSION_FUEL_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
			this.resultsList.addChild(new ResultWidget(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translate(), VolumeTextHelper.formatMB(heatedCoolantCapacity)));
			this.resultsList.addChild(new ResultWidget(GeneratorsLang.FISSION_WASTE_TANK.translate(), VolumeTextHelper.formatMB(fuelCapacity)));
		}

		if (this.resultsList.getChildUnderMouse(mouseX, mouseY) instanceof ResultWidget result)
		{
			Component[] tooltip = result.getJeiTooltip();
			guiGraphics.renderComponentTooltip(this.font, Arrays.asList(tooltip), mouseX, mouseY);
		}

	}

	private void updateLayout()
	{
		this.layoutDirty = false;
		this.resultDirty = true;
		this.simulation.setAdvanced(true);
		this.simulation.setAdvancedLayout(this.layout);
		this.simulation.updateBurnRateSliderLimit();
		this.burnRateSlider.getSlider().setMaxValue(this.simulation.getMaxBurnRate());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		this.setImportButtonResult(Result.NONE);
		this.setExportButtonResult(Result.NONE);

		Layout layout = this.layout;
		int x = (int) (mouseX - GRID_X) / CELL_WIDTH;
		int z = (int) (mouseY - GRID_Y) / CELL_HEIGHT;

		if (layout.canPillar(x, z))
		{
			if (button == 0)
			{
				layout.setPillar(x, z, layout.getPillarMax());
				this.layoutDirty = true;
			}
			else if (button == 1)
			{
				layout.setPillar(x, z, 0);
				this.layoutDirty = true;
			}
			else if (button == 2)
			{
				this.dragPillar = layout.getPillar(x, z);
			}

			this.prevX = x;
			this.prevZ = z;
			this.dragging = true;
			return true;
		}
		else
		{
			this.dragging = false;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		Layout layout = this.layout;
		int x = (int) (mouseX - GRID_X) / CELL_WIDTH;
		int z = (int) (mouseY - GRID_Y) / CELL_HEIGHT;

		if (layout.canPillar(x, z))
		{
			int prev = layout.getPillar(x, z);
			int next = scrollY > 0.0D ? (prev + 1) : (prev - 1);
			layout.setPillar(x, z, next);
			this.layoutDirty = true;
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		Layout layout = this.layout;
		int x = (int) (mouseX - GRID_X) / CELL_WIDTH;
		int z = (int) (mouseY - GRID_Y) / CELL_HEIGHT;

		if (this.dragging && (this.prevX != x || this.prevZ != z))
		{
			if (layout.canPillar(x, z))
			{
				if (button == 0)
				{
					layout.setPillar(x, z, layout.getPillarMax());
					this.layoutDirty = true;
				}
				else if (button == 1)
				{
					layout.setPillar(x, z, 0);
					this.layoutDirty = true;
				}
				else if (button == 2)
				{
					layout.setPillar(x, z, this.dragPillar);
					this.layoutDirty = true;
				}

				this.prevX = x;
				this.prevZ = z;
				return true;
			}

		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	private void onBurnRateChanged(long burnRate)
	{
		this.simulation.setBurnRate(burnRate);
		this.burnRate = burnRate;
		this.resultDirty = true;
	}

	private void onDimensionWidthChanged(int width)
	{
		this.layout.setWidth(width);
		this.layoutDirty = true;
	}

	private void onDimensionLengthChanged(int length)
	{
		this.layout.setLength(length);
		this.layoutDirty = true;
	}

	private void onDimensionHeightChanged(int height)
	{
		int prevPillarMax = this.layout.getPillarMax();
		this.layout.setHeight(height);
		int nextPillarMax = this.layout.getPillarMax();

		for (int z = 1; z < this.layout.getLength() - 1; z++)
		{
			for (int x = 1; x < this.layout.getWidth() - 1; x++)
			{
				int prevPillar = this.layout.getPillar(x, z);
				this.layout.setPillar(x, z, (prevPillar == prevPillarMax) ? nextPillarMax : prevPillar);
			}

		}

		this.layoutDirty = true;
	}

	private void onButtonPress(AbstractButton button)
	{
		if (button == this.resetButton)
		{
			this.layout.resetPillars();
			this.layoutDirty = true;
		}
		else if (button == this.clearButton)
		{
			this.layout.clearPillars();
			this.layoutDirty = true;
		}
		else if (button == this.saveButton)
		{
			this.onClose();
			this.widget.setAdvanced(true);
			this.widget.setAdvancedLayout(this.layout);
			this.widget.setBurnRate(this.burnRate);
		}
		else if (button == this.importButton)
		{
			try
			{
				String clipboard = this.minecraft.keyboardHandler.getClipboard();
				JsonObject jobject = (JsonObject) JsonParser.parseString(clipboard);

				this.widthSlider.getSlider().setValue(GsonHelper.getAsInt(jobject, "width"));
				this.lengthSlider.getSlider().setValue(GsonHelper.getAsInt(jobject, "length"));
				this.heightSlider.getSlider().setValue(GsonHelper.getAsInt(jobject, "height"));

				JsonArray lines = GsonHelper.getAsJsonArray(jobject, "grid", new JsonArray());
				int innerLength = Math.min(lines.size(), this.layout.getLength() - 2);

				for (int z = 0; z < innerLength; z++)
				{
					String[] line = lines.get(z).getAsString().replace(" ", "").split(",");
					int innerWidth = Math.min(line.length, this.layout.getWidth() - 2);

					for (int x = 0; x < innerWidth; x++)
					{
						this.layout.setPillar(1 + x, 1 + z, Integer.parseInt(line[x]));
					}

				}

				this.updateLayout();
				this.burnRateSlider.getSlider().setValue(GsonHelper.getAsInt(jobject, "burnRate"));
				this.setImportButtonResult(Result.SUCCESS);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.updateLayout();
				this.setImportButtonResult(Result.ERROR);
			}

		}
		else if (button == this.exportButton)
		{
			try
			{
				JsonObject jobject = new JsonObject();
				jobject.addProperty("width", this.layout.getWidth());
				jobject.addProperty("length", this.layout.getLength());
				jobject.addProperty("height", this.layout.getHeight());
				jobject.addProperty("burnRate", this.burnRate);

				JsonArray lines = new JsonArray();
				jobject.add("grid", lines);

				DecimalFormat format = new DecimalFormat();
				format.setMinimumIntegerDigits((int) Math.ceil(Math.log10(this.layout.getPillarMax() + 1)));

				int innerLength = this.layout.getLength() - 2;
				int innerWidth = this.layout.getWidth() - 2;

				for (int z = 0; z < innerLength; z++)
				{
					StringBuilder line = new StringBuilder();

					for (int x = 0; x < innerWidth; x++)
					{
						if (x > 0)
						{
							line.append(",");
						}

						line.append(format.format(this.layout.getPillar(1 + x, 1 + z)));
					}

					lines.add(line.toString());
				}

				this.minecraft.keyboardHandler.setClipboard(GSON.toJson(jobject));
				this.setExportButtonResult(Result.SUCCESS);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.setExportButtonResult(Result.ERROR);
			}

		}

	}

	private void setImportButtonResult(Result result)
	{
		if (this.importResult == result)
		{
			return;
		}

		this.importResult = result;

		if (result == Result.SUCCESS)
		{
			this.importButton.setMessage(COMPONENT_CHECK);
			this.importButton.setFGColor(0xFF00FF00);
			this.importButton.setTooltip(Tooltip.create(COMPONENT_IMPORTED_SUCCESSFULLY));
		}
		else if (result == Result.ERROR)
		{
			this.importButton.setMessage(COMPONENT_ERROR);
			this.importButton.setFGColor(0xFFFF0000);
			this.importButton.setTooltip(Tooltip.create(COMPONENT_JSON_SYNTAX_ERROR));
		}
		else
		{
			this.importButton.setMessage(COMPONENT_IMPORT);
			this.importButton.setFGColor(0xFFFFFFFF);
			this.importButton.setTooltip(Tooltip.create(COMPONENT_IMPORT_FROM_CLIPBOARD));
		}

	}

	private void setExportButtonResult(Result result)
	{
		if (this.exportResult == result)
		{
			return;
		}

		this.exportResult = result;

		if (result == Result.SUCCESS)
		{
			this.exportButton.setMessage(COMPONENT_CHECK);
			this.exportButton.setFGColor(0xFF00FF00);
			this.exportButton.setTooltip(Tooltip.create(COMPONENT_EXPORTED_SUCCESSFULLY));
		}
		else if (result == Result.ERROR)
		{
			this.exportButton.setMessage(COMPONENT_ERROR);
			this.exportButton.setFGColor(0xFFFF0000);
			this.exportButton.setTooltip(Tooltip.create(Component.translatable("ERROR")));
		}
		else
		{
			this.exportButton.setMessage(COMPONENT_EXPORT);
			this.exportButton.setFGColor(0xFFFFFFFF);
			this.exportButton.setTooltip(Tooltip.create(COMPONENT_EXPORT_TO_CLIPBOARD));
		}

	}

	enum Result
	{
		NONE,
		SUCCESS,
		ERROR
	}

}
