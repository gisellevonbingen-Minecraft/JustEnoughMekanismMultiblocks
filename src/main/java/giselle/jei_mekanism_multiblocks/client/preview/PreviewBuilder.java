package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PreviewBuilder implements IPreviewBuilder
{
	private final Vec3i dimension;
	private final Map<BlockPos, BlockState> blocks;

	public PreviewBuilder(Vec3i dimension)
	{
		this.dimension = dimension;
		this.blocks = new LinkedHashMap<>();
	}

	@Override
	public Vec3i getDimension()
	{
		return this.dimension;
	}

	@Override
	public BlockState getBlock(BlockPos pos)
	{
		if (this.isOutOfDimension(pos))
		{
			return Blocks.AIR.defaultBlockState();
		}

		return this.blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	public Map<BlockPos, BlockState> getBlocks()
	{
		return new HashMap<>(this.blocks);
	}

	@Override
	public void setBlock(BlockPos pos, BlockState state)
	{
		if (this.isOutOfDimension(pos))
		{
			return;
		}

		if (state.isAir())
		{
			this.blocks.remove(pos);
		}
		else
		{
			this.blocks.put(pos.immutable(), state);
		}

	}

	@Override
	public void setBlock(PreviewSelector selector, BlockState state, int limit)
	{
		List<BlockPos> candidates = selector.select(this);
		int size = candidates.size();
		int count = 0;

		for (int i = 0; (i < size) && (count < limit); i++)
		{
			this.setBlock(candidates.get(i), state);
			count++;
		}

	}

	@Override
	public void replaceBlock(PreviewSelector selector, BlockState from, BlockState to, int limit)
	{
		List<BlockPos> candidates = selector.select(this);
		int size = candidates.size();
		int count = 0;

		for (int i = 0; (i < size) && (count < limit); i++)
		{
			BlockPos pos = candidates.get(i);

			if (this.getBlock(pos).equals(from))
			{
				this.setBlock(pos, to);
				count++;
			}

		}

	}

}
