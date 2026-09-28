package giselle.jei_mekanism_multiblocks.client.preview;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;

public class PreviewMesh implements AutoCloseable
{
	private static final RenderType RENDER_TYPE = RenderType.translucent();

	private final PreviewLevel level;
	private final BlockPos renderMin;
	private final BlockPos renderMax;
	private final RandomSource randomSource;

	private VertexBuffer vertexBuffer;
	private MeshData.SortState sortState;
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

		try (ByteBufferBuilder vertices = new ByteBufferBuilder(RENDER_TYPE.bufferSize()); ByteBufferBuilder indices = new ByteBufferBuilder(RENDER_TYPE.bufferSize()))
		{
			BufferBuilder builder = new BufferBuilder(vertices, RENDER_TYPE.mode(), RENDER_TYPE.format());
			PoseStack blockPose = new PoseStack();

			for (BlockPos pos : BlockPos.betweenClosed(this.renderMin, this.renderMax))
			{
				blockPose.pushPose();
				blockPose.translate(pos.getX(), pos.getY(), pos.getZ());
				renderer.renderBatched(this.level.getBlockState(pos), pos, this.level, blockPose, builder, true, this.randomSource, ModelData.EMPTY, null);
				blockPose.popPose();
			}

			VertexSorting sorting = RenderSystem.getVertexSorting();
			MeshData mesh = builder.build();

			if (mesh != null)
			{
				try
				{
					this.sortState = mesh.sortQuads(indices, transformedSorting(pose, sorting));
					this.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
					this.vertexBuffer.bind();
					MeshData uploadMesh = mesh;
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
						mesh.close();
					}

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

		try (ByteBufferBuilder indices = new ByteBufferBuilder(RENDER_TYPE.bufferSize()))
		{
			ByteBufferBuilder.Result result = this.sortState.buildSortedIndexBuffer(indices, transformedSorting(pose, sorting));

			try
			{
				this.vertexBuffer.bind();
				ByteBufferBuilder.Result uploadResult = result;
				result = null;
				this.vertexBuffer.uploadIndexBuffer(uploadResult);
			}
			finally
			{
				VertexBuffer.unbind();

				if (result != null)
				{
					result.close();
				}

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
		this.sortedPose = null;
		this.sortedWith = null;
		this.compiledHeight = -1;
	}

}
