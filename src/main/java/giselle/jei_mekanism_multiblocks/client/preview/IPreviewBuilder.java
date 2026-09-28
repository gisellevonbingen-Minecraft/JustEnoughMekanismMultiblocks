package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public interface IPreviewBuilder
{
	Vec3i getDimension();

	default boolean isOutOfDimension(BlockPos pos)
	{
		Vec3i dimension = this.getDimension();
		return pos.getX() < 0 || pos.getX() >= dimension.getX() || pos.getY() < 0 || pos.getY() >= dimension.getY() || pos.getZ() < 0 || pos.getZ() >= dimension.getZ();
	}

	BlockState getBlock(BlockPos pos);

	Map<BlockPos, BlockState> getBlocks();

	void setBlock(BlockPos pos, BlockState state);

	default void setBlockShell(BlockState edgeState, BlockState sideState)
	{
		this.setBlock(PreviewSelectors.edges(), edgeState);
		this.setBlock(PreviewSelectors.sides(), sideState);
	}

	default void setBlock(PreviewSelector selector, BlockState state)
	{
		this.setBlock(selector, state, Integer.MAX_VALUE);
	}

	void setBlock(PreviewSelector selector, BlockState state, int limit);

	default void replaceBlock(PreviewSelector selector, BlockState from, BlockState to)
	{
		this.replaceBlock(selector, from, to, Integer.MAX_VALUE);
	}

	void replaceBlock(PreviewSelector selector, BlockState from, BlockState to, int limit);
}
