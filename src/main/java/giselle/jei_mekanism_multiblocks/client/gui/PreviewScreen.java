package giselle.jei_mekanism_multiblocks.client.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import giselle.jei_mekanism_multiblocks.client.preview.PreviewLevel;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PreviewScreen extends WidgetScreen
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
	private BlockHitResult hit;

	public PreviewScreen(Component title, PreviewLevel level)
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
				this.getSlider().setMessage(Component.translatable("text.jei_mekanism_multiblocks.preview.layers", this.getDisplayValue(), this.getSlider().getMaxValue()));
			}
		};
		this.renderHeightSilder.getSlider().addValueChangeHanlder(this::onRenderHeightChanged);
		this.addRenderableWidget(this.renderHeightSilder);

		this.resetButton = new ButtonWidget(sliderX, sliderY + sliderHeight, sliderWidth, sliderHeight, Component.translatable("text.jei_mekanism_multiblocks.reset"));
		this.resetButton.addPressHandler(this::onButtonPress);
		this.addRenderableWidget(this.resetButton);

		this.resetView();
	}

	private void resetView()
	{
		Vec3i dimension = this.level.getDimension();
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

	private void onButtonPress(ButtonWidget button)
	{
		if (button == this.resetButton)
		{
			this.resetView();
		}

	}

	@Override
	protected void renderForeground(PoseStack pose, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.renderForeground(pose, pMouseX, pMouseY, pPartialTicks);

		Vec3i dimension = this.level.getDimension();
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

		pose.pushPose();
		pose.translate(this.translationX, -this.translationY, 0.0F);
		pose.translate(this.width * 0.5F, this.height * 0.5F, 0.0F);
		pose.scale(this.scale, -this.scale, this.scale);
		pose.mulPose(Vector3f.XP.rotationDegrees(this.rotationX));
		pose.mulPose(Vector3f.YP.rotationDegrees(this.rotationY));
		pose.translate(-dimension.getX() * 0.5F, -dimension.getY() * 0.5F, -dimension.getZ() * 0.5F);

		this.hit = this.pickBlock(pose, pMouseX, pMouseY);

		this.mesh.render(pose, this.minecraft.getBlockRenderer());
		pose.popPose();

	}

	@Override
	protected void renderTooltip(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		super.renderTooltip(pPoseStack, pMouseX, pMouseY, pPartialTicks);

		if (this.hit != null && this.hit.getType() == HitResult.Type.BLOCK)
		{
			BlockPos pos = this.hit.getBlockPos();
			BlockState state = this.level.getBlockState(pos);
			this.renderTooltip(pPoseStack, new ItemStack(state.getBlock()), pMouseX, pMouseY);
		}

	}

	private BlockHitResult pickBlock(PoseStack pose, int mouseX, int mouseY)
	{
		Matrix4f inverse = RenderSystem.getProjectionMatrix().copy();
		inverse.multiply(RenderSystem.getModelViewMatrix());
		inverse.multiply(pose.last().pose());
		inverse.invert();
		float x = 2.0F * mouseX / this.width - 1.0F;
		float y = 1.0F - 2.0F * mouseY / this.height;
		Vec3 from = unproject(inverse, x, y, -1.0F);
		Vec3 to = unproject(inverse, x, y, 1.0F);
		return this.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
	}

	private static Vec3 unproject(Matrix4f inverse, float x, float y, float z)
	{
		Vector4f point = new Vector4f(x, y, z, 1.0F);
		point.transform(inverse);
		point.perspectiveDivide();
		return new Vec3(point.x(), point.y(), point.z());
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
			this.rotationX = Mth.clamp(this.rotationX + (float) dragY, 0.0F, 90.0F);
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
