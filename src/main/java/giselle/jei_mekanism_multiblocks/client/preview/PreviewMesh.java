package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class PreviewMesh implements AutoCloseable
{
	private static final RenderType RENDER_TYPE = RenderType.translucent();
	private static final float SORT_DISTANCE = 10000.0F;

	private final PreviewLevel level;
	private final BlockPos renderMin;
	private final BlockPos renderMax;
	private final Random randomSource;

	private VertexBuffer vertexBuffer;
	private BufferBuilder.State sortState;
	private Matrix4f sortedPose;
	private int compiledHeight = -1;

	public PreviewMesh(PreviewLevel level)
	{
		this.level = level;
		Vector3i dimension = level.getDimension();
		this.renderMin = BlockPos.ZERO;
		this.renderMax = new BlockPos(dimension.getX() - 1, dimension.getY() - 1, dimension.getZ() - 1);
		this.randomSource = new Random(0L);
	}

	public void render(MatrixStack pose, BlockRendererDispatcher renderer)
	{
		if (this.compiledHeight != this.level.getRenderHeight())
		{
			this.compile(renderer, pose.last().pose());
		}

		if (this.vertexBuffer == null)
		{
			return;
		}

		this.resortIfNeeded(pose.last().pose());

		try
		{
			RENDER_TYPE.setupRenderState();
			this.vertexBuffer.bind();
			RENDER_TYPE.format().setupBufferState(0L);
			Matrix4f modelView = Matrix4f.createTranslateMatrix(0.0F, 0.0F, 1000.0F - ForgeHooksClient.getGuiFarPlane());
			modelView.multiply(pose.last().pose());
			this.vertexBuffer.draw(modelView, RENDER_TYPE.mode());
		}
		finally
		{
			RENDER_TYPE.format().clearBufferState();
			VertexBuffer.unbind();
			RENDER_TYPE.clearRenderState();
		}

	}

	private void compile(BlockRendererDispatcher renderer, Matrix4f pose)
	{
		this.close();

		BufferBuilder builder = new BufferBuilder(RENDER_TYPE.bufferSize());
		builder.begin(RENDER_TYPE.mode(), RENDER_TYPE.format());
		MatrixStack blockPose = new MatrixStack();

		for (BlockPos pos : BlockPos.betweenClosed(this.renderMin, this.renderMax))
		{
			blockPose.pushPose();
			blockPose.translate(pos.getX(), pos.getY(), pos.getZ());
			renderer.renderModel(this.level.getBlockState(pos), pos, this.level, blockPose, builder, true, this.randomSource, EmptyModelData.INSTANCE);
			blockPose.popPose();
		}

		Vector4f sortOrigin = this.sortOrigin(pose);
		builder.sortQuads(sortOrigin.x(), sortOrigin.y(), sortOrigin.z());
		this.sortState = builder.getState();
		builder.end();
		this.vertexBuffer = new VertexBuffer(RENDER_TYPE.format());
		this.vertexBuffer.upload(builder);
		this.sortedPose = pose.copy();
		this.compiledHeight = this.level.getRenderHeight();
	}

	private void resortIfNeeded(Matrix4f pose)
	{
		if (this.sortState == null || pose.equals(this.sortedPose))
		{
			return;
		}

		BufferBuilder builder = new BufferBuilder(RENDER_TYPE.bufferSize());
		builder.begin(RENDER_TYPE.mode(), RENDER_TYPE.format());
		builder.restoreState(this.sortState);
		Vector4f sortOrigin = this.sortOrigin(pose);
		builder.sortQuads(sortOrigin.x(), sortOrigin.y(), sortOrigin.z());
		this.sortState = builder.getState();
		builder.end();
		this.vertexBuffer.upload(builder);
		this.sortedPose = pose.copy();
	}

	private Vector4f sortOrigin(Matrix4f pose)
	{
		Matrix4f inverse = pose.copy();
		inverse.invert();
		Vector4f direction = new Vector4f(0.0F, 0.0F, 1.0F, 0.0F);
		direction.transform(inverse);
		float length = (float) Math.sqrt(direction.x() * direction.x() + direction.y() * direction.y() + direction.z() * direction.z());
		Vector3i dimension = this.level.getDimension();
		return new Vector4f(dimension.getX() * 0.5F + direction.x() * SORT_DISTANCE / length, dimension.getY() * 0.5F + direction.y() * SORT_DISTANCE / length, dimension.getZ() * 0.5F + direction.z() * SORT_DISTANCE / length, 1.0F);
	}

	@Override
	public void close()
	{
		if (this.vertexBuffer != null)
		{
			this.vertexBuffer.close();
			this.vertexBuffer = null;
		}

		this.sortState = null;
		this.sortedPose = null;
		this.compiledHeight = -1;
	}

}
