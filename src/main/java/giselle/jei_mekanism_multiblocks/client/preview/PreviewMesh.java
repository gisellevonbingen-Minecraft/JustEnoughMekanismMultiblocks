package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.model.data.EmptyModelData;

public class PreviewMesh implements AutoCloseable
{
	private static final RenderType RENDER_TYPE = RenderType.translucent();
	private static final float SORT_DISTANCE = 10000.0F;

	private final PreviewLevel level;
	private final BlockPos renderMin;
	private final BlockPos renderMax;
	private final Random randomSource;

	private BufferBuilder builder;
	private VertexBuffer vertexBuffer;
	private BufferBuilder.SortState sortState;
	private Matrix4f sortedPose;
	private int compiledHeight = -1;

	public PreviewMesh(PreviewLevel level)
	{
		this.level = level;
		Vec3i dimension = level.getDimension();
		this.renderMin = BlockPos.ZERO;
		this.renderMax = new BlockPos(dimension.getX() - 1, dimension.getY() - 1, dimension.getZ() - 1);
		this.randomSource = new Random(0L);
	}

	public void render(PoseStack pose, BlockRenderDispatcher renderer)
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
			Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
			modelView.multiply(pose.last().pose());
			this.vertexBuffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
		}
		finally
		{
			VertexBuffer.unbind();
			RENDER_TYPE.clearRenderState();
		}

	}

	private void compile(BlockRenderDispatcher renderer, Matrix4f pose)
	{
		this.close();

		BufferBuilder builder = this.builder = new BufferBuilder(RENDER_TYPE.bufferSize());
		builder.begin(RENDER_TYPE.mode(), RENDER_TYPE.format());
		PoseStack blockPose = new PoseStack();

		for (BlockPos pos : BlockPos.betweenClosed(this.renderMin, this.renderMax))
		{
			blockPose.pushPose();
			blockPose.translate(pos.getX(), pos.getY(), pos.getZ());
			renderer.renderBatched(this.level.getBlockState(pos), pos, this.level, blockPose, builder, true, this.randomSource, EmptyModelData.INSTANCE);
			blockPose.popPose();
		}

		Vector4f sortOrigin = this.sortOrigin(pose);
		builder.setQuadSortOrigin(sortOrigin.x(), sortOrigin.y(), sortOrigin.z());
		this.sortState = builder.getSortState();
		builder.end();
		this.upload(builder, pose);
		this.compiledHeight = this.level.getRenderHeight();
	}

	private void resortIfNeeded(Matrix4f pose)
	{
		if (this.sortState == null || pose.equals(this.sortedPose))
		{
			return;
		}

		BufferBuilder builder = this.builder;
		builder.begin(RENDER_TYPE.mode(), RENDER_TYPE.format());
		builder.restoreSortState(this.sortState);
		Vector4f sortOrigin = this.sortOrigin(pose);
		builder.setQuadSortOrigin(sortOrigin.x(), sortOrigin.y(), sortOrigin.z());
		builder.end();
		this.upload(builder, pose);
	}

	private void upload(BufferBuilder builder, Matrix4f pose)
	{
		try
		{
			if (this.vertexBuffer == null)
			{
				this.vertexBuffer = new VertexBuffer();
			}

			this.vertexBuffer.bind();
			this.vertexBuffer.upload(builder);
		}
		finally
		{
			VertexBuffer.unbind();
		}

		this.sortedPose = pose.copy();
	}

	private Vector4f sortOrigin(Matrix4f pose)
	{
		Matrix4f inverse = pose.copy();
		inverse.invert();
		Vector4f direction = new Vector4f(0.0F, 0.0F, 1.0F, 0.0F);
		direction.transform(inverse);
		float length = (float) Math.sqrt(direction.x() * direction.x() + direction.y() * direction.y() + direction.z() * direction.z());
		Vec3i dimension = this.level.getDimension();
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

		this.builder = null;
		this.sortState = null;
		this.sortedPose = null;
		this.compiledHeight = -1;
	}

}
