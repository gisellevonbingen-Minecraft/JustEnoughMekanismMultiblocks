package giselle.jei_mekanism_multiblocks.client.preview;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;

public class PreviewMesh implements AutoCloseable
{
	private static final RenderType RENDER_TYPE = RenderType.translucent();

	private final PreviewLevel level;
	private final BlockPos renderMin;
	private final BlockPos renderMax;
	private final RandomSource randomSource;

	private BufferBuilder builder;
	private VertexBuffer vertexBuffer;
	private BufferBuilder.SortState sortState;
	private Matrix4f sortedPose;
	private VertexSorting sortedWith;
	private int compiledHeight = -1;

	public PreviewMesh(PreviewLevel level)
	{
		this.level = level;
		Vec3i dimension = level.getDimension();
		this.renderMin = BlockPos.ZERO;
		this.renderMax = new BlockPos(dimension.getX() - 1, dimension.getY() - 1, dimension.getZ() - 1);
		this.randomSource = RandomSource.create(0L);
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
			modelView.mul(pose.last().pose());
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
			renderer.renderBatched(this.level.getBlockState(pos), pos, this.level, blockPose, builder, true, this.randomSource, ModelData.EMPTY, null);
			blockPose.popPose();
		}

		VertexSorting sorting = RenderSystem.getVertexSorting();
		builder.setQuadSorting(transformedSorting(pose, sorting));
		this.sortState = builder.getSortState();
		BufferBuilder.RenderedBuffer mesh = builder.endOrDiscardIfEmpty();

		if (mesh != null)
		{
			try
			{
				this.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
				this.vertexBuffer.bind();
				BufferBuilder.RenderedBuffer uploadMesh = mesh;
				mesh = null;

				this.vertexBuffer.upload(uploadMesh);
				this.sortedPose = new Matrix4f(pose);
				this.sortedWith = sorting;
			}
			finally
			{
				VertexBuffer.unbind();

				if (mesh != null)
				{
					mesh.release();
				}

			}

		}

		this.compiledHeight = this.level.getRenderHeight();
	}

	private void resortIfNeeded(Matrix4f pose)
	{
		VertexSorting sorting = RenderSystem.getVertexSorting();

		if (this.sortState == null || (pose.equals(this.sortedPose) && sorting == this.sortedWith))
		{
			return;
		}

		BufferBuilder builder = this.builder;
		builder.begin(RENDER_TYPE.mode(), RENDER_TYPE.format());
		builder.restoreSortState(this.sortState);
		builder.setQuadSorting(transformedSorting(pose, sorting));
		BufferBuilder.RenderedBuffer result = builder.endOrDiscardIfEmpty();

		try
		{
			this.vertexBuffer.bind();
			BufferBuilder.RenderedBuffer uploadResult = result;
			result = null;
			this.vertexBuffer.upload(uploadResult);
		}
		finally
		{
			VertexBuffer.unbind();

			if (result != null)
			{
				result.release();
			}

		}

		this.sortedPose = new Matrix4f(pose);
		this.sortedWith = sorting;
	}

	private static VertexSorting transformedSorting(Matrix4f pose, VertexSorting sorting)
	{
		return centroids ->
		{
			Vector3f[] transformed = new Vector3f[centroids.length];

			for (int i = 0; i < centroids.length; i++)
			{
				transformed[i] = new Vector3f(centroids[i]).mulPosition(pose);
			}

			return sorting.sort(transformed);
		};
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
		this.builder = null;
		this.sortedPose = null;
		this.sortedWith = null;
		this.compiledHeight = -1;
	}

}
