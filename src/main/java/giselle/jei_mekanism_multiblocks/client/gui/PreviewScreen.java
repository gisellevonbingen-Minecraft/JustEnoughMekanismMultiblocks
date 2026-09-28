package giselle.jei_mekanism_multiblocks.client.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import giselle.jei_mekanism_multiblocks.client.preview.PreviewLevel;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewMesh;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;

public class PreviewScreen extends Screen
{
	private final PreviewLevel level;
	private final PreviewMesh mesh;

	private IntSliderWithButtons renderHeightSilder;
	private ButtonWidget resetButton;

	private float scale;
	private float rotationY;
	private float rotationX;
	private float translationX;
	private float translationY;

	public PreviewScreen(ITextComponent title, PreviewLevel level)
	{
		super(title);

		this.level = level;
		this.mesh = new PreviewMesh(level);
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@Override
	public void removed()
	{
		this.mesh.close();
		super.removed();
	}

	@Override
	protected void init()
	{
		super.init();

		int sliderWidth = 100;
		int sliderHeight = 15;
		int sliderX = this.width - sliderWidth - 5;
		int sliderY = this.height / 2 - sliderHeight;
		this.renderHeightSilder = new IntSliderWithButtons(sliderX, sliderY, sliderWidth, sliderHeight, "", this.level.getRenderHeight(), 1, this.level.getDimension().getY())
		{
			@Override
			protected void updateMessage()
			{
				this.getSlider().setMessage(new TranslationTextComponent("text.jei_mekanism_multiblocks.preview.layers", this.getDisplayValue(), this.getSlider().getMaxValue()));
			}
		};
		this.renderHeightSilder.getSlider().addValueChangeHanlder(this::onRenderHeightChanged);
		this.addButton(this.renderHeightSilder);

		this.resetButton = new ButtonWidget(sliderX, sliderY + sliderHeight, sliderWidth, sliderHeight, new TranslationTextComponent("text.jei_mekanism_multiblocks.reset"));
		this.resetButton.addPressHandler(this::onButtonPress);
		this.addButton(this.resetButton);

		this.resetView();
	}

	private void resetView()
	{
		Vector3i dimension = this.level.getDimension();
		float scaleByHeight = this.height / (float) Math.sqrt(dimension.distSqr(BlockPos.ZERO));
		float scaleByWidth = this.renderHeightSilder.x / (float) Math.sqrt(dimension.distSqr(BlockPos.ZERO.above(dimension.getY())));
		this.scale = Math.min(scaleByHeight, scaleByWidth);
		this.translationX = (this.renderHeightSilder.x - this.width) / 2;
		this.translationY = 0.0F;
		this.rotationY = -22.5F;
		this.rotationX = 22.5F;
		this.renderHeightSilder.getSlider().setValue(this.renderHeightSilder.getSlider().getMaxValue());
	}

	private void onRenderHeightChanged(int renderHeight)
	{
		this.level.setRenderHeight(renderHeight);
	}

	private void onButtonPress(AbstractButton button)
	{
		if (button == this.resetButton)
		{
			this.resetView();
		}

	}

	@Override
	public void render(MatrixStack pose, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(pose);

		super.render(pose, mouseX, mouseY, partialTick);

		Vector3i dimension = this.level.getDimension();
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

		pose.pushPose();
		pose.translate(this.translationX, -this.translationY, 0.0F);
		pose.translate(this.width * 0.5F, this.height * 0.5F, 0.0F);
		pose.scale(this.scale, -this.scale, this.scale);
		pose.mulPose(Vector3f.XP.rotationDegrees(this.rotationX));
		pose.mulPose(Vector3f.YP.rotationDegrees(this.rotationY));
		pose.translate(-dimension.getX() * 0.5F, -dimension.getY() * 0.5F, -dimension.getZ() * 0.5F);

		BlockRayTraceResult hit = this.pickBlock(pose, mouseX, mouseY);

		this.mesh.render(pose, this.minecraft.getBlockRenderer());
		pose.popPose();

		if (hit.getType() == RayTraceResult.Type.BLOCK)
		{
			BlockPos pos = hit.getBlockPos();
			BlockState state = this.level.getBlockState(pos);
			this.renderTooltip(pose, new ItemStack(state.getBlock()), mouseX, mouseY);
		}

	}

	private BlockRayTraceResult pickBlock(MatrixStack pose, int mouseX, int mouseY)
	{
		Matrix4f inverse = pose.last().pose().copy();
		inverse.invert();
		float guiNearPlane = 1000.0F;
		float guiFarPlane = ForgeHooksClient.getGuiFarPlane();
		Vector3d from = unproject(inverse, mouseX, mouseY, guiFarPlane - 2.0F * guiNearPlane);
		Vector3d to = unproject(inverse, mouseX, mouseY, -guiNearPlane);
		return this.level.clip(new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
	}

	private static Vector3d unproject(Matrix4f inverse, float x, float y, float z)
	{
		Vector4f point = new Vector4f(x, y, z, 1.0F);
		point.transform(inverse);
		point.perspectiveDivide();
		return new Vector3d(point.x(), point.y(), point.z());
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
		{
			return true;
		}

		if (button == 0)
		{
			this.rotationX = MathHelper.clamp(this.rotationX + (float) dragY, 0.0F, 90.0F);
			this.rotationY += (float) dragX;
		}

		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (keyCode == GLFW.GLFW_KEY_R)
		{
			this.resetView();
			return true;
		}
		else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_DOWN)
		{
			int delta = Screen.hasShiftDown() ? SliderWithButtons.SHIFT_DELTA : SliderWithButtons.NORMAL_DELTA;
			this.renderHeightSilder.getSlider().setValue(this.renderHeightSilder.getSlider().getValue() - delta);
			return true;
		}
		else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_UP)
		{
			int delta = Screen.hasShiftDown() ? SliderWithButtons.SHIFT_DELTA : SliderWithButtons.NORMAL_DELTA;
			this.renderHeightSilder.getSlider().setValue(this.renderHeightSilder.getSlider().getValue() + delta);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

}
