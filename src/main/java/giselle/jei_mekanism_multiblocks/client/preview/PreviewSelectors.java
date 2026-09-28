package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public final class PreviewSelectors
{
	private PreviewSelectors()
	{

	}

	private static List<BlockPos> selectBoundary(Vector3i dimension, IntPredicate boundaryPredicate)
	{
		int maxX = dimension.getX() - 1;
		int maxY = dimension.getY() - 1;
		int maxZ = dimension.getZ() - 1;
		List<BlockPos> positions = new ArrayList<>();

		for (int y = 0; y <= maxY; y++)
		{
			for (int x = 0; x <= maxX; x++)
			{
				for (int z = 0; z <= maxZ; z++)
				{
					int boundaries = 0;
					boundaries += x == 0 || x == maxX ? 1 : 0;
					boundaries += y == 0 || y == maxY ? 1 : 0;
					boundaries += z == 0 || z == maxZ ? 1 : 0;

					if (boundaryPredicate.test(boundaries))
					{
						positions.add(new BlockPos(x, y, z));
					}

				}

			}

		}

		return positions;
	}

	public static PreviewSelector edges()
	{
		return builder -> selectBoundary(builder.getDimension(), boundaries -> boundaries >= 2);
	}

	public static PreviewSelector sides()
	{
		return builder -> selectBoundary(builder.getDimension(), boundaries -> boundaries == 1);
	}

	public static PreviewSelector shellPattern(byte[][] pattern, IntPredicate valuePredicate)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int maxX = dimension.getX() - 1;
			int maxY = dimension.getY() - 1;
			int maxZ = dimension.getZ() - 1;
			List<BlockPos> positions = new ArrayList<>();

			for (int y = 0; y <= maxY; y++)
			{
				for (int x = 0; x <= maxX; x++)
				{
					for (int z = 0; z <= maxZ; z++)
					{
						int horizontal;
						int vertical;

						if (x == 0 || x == maxX)
						{
							horizontal = z;
							vertical = y;
						}
						else if (z == 0 || z == maxZ)
						{
							horizontal = x;
							vertical = y;
						}
						else if (y == 0 || y == maxY)
						{
							horizontal = x;
							vertical = z;
						}
						else
						{
							continue;
						}

						if (valuePredicate.test(pattern[horizontal][vertical]))
						{
							positions.add(new BlockPos(x, y, z));
						}

					}

				}

			}

			return positions;
		};
	}

	public static PreviewSelector volumePattern(byte[][][] pattern, IntPredicate valuePredicate)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			List<BlockPos> positions = new ArrayList<>();
			int height = Math.min(pattern.length, dimension.getY());

			for (int y = 0; y < height; y++)
			{
				byte[][] layer = pattern[y];
				int length = Math.min(layer.length, dimension.getZ());

				for (int z = 0; z < length; z++)
				{
					byte[] row = layer[z];
					int width = Math.min(row.length, dimension.getX());

					for (int x = 0; x < width; x++)
					{
						if (valuePredicate.test(row[x]))
						{
							positions.add(new BlockPos(x, y, z));
						}

					}

				}

			}

			return positions;
		};
	}

	public static PreviewSelector plane(ToIntFunction<IPreviewBuilder> atY)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int y = atY.applyAsInt(builder);
			List<BlockPos> positions = new ArrayList<>();

			for (int x = 0; x < dimension.getX(); x++)
			{
				for (int z = 0; z < dimension.getZ(); z++)
				{
					positions.add(new BlockPos(x, y, z));
				}

			}

			return positions;
		};
	}

	public static PreviewSelector top()
	{
		return plane(builder -> builder.getDimension().getY() - 1);
	}

	public static PreviewSelector toTop(ToIntFunction<IPreviewBuilder> yFunction)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int bottom = yFunction.applyAsInt(builder);
			int top = dimension.getY() - 1;
			List<BlockPos> positions = new ArrayList<>();

			for (int y = bottom; y <= top; y++)
			{
				for (int x = 0; x < dimension.getX(); x++)
				{
					for (int z = 0; z < dimension.getZ(); z++)
					{
						positions.add(new BlockPos(x, y, z));
					}

				}

			}

			return positions;
		};
	}

	public static PreviewSelector innerPlane(ToIntFunction<IPreviewBuilder> yFunction)
	{
		return innerCube(yFunction, builder -> 1);
	}

	public static PreviewSelector innerCube(ToIntFunction<IPreviewBuilder> yFunction, ToIntFunction<IPreviewBuilder> heightFunction)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int bottom = yFunction.applyAsInt(builder);
			int top = bottom + heightFunction.applyAsInt(builder) - 1;
			List<BlockPos> positions = new ArrayList<>();

			for (int y = bottom; y <= top; y++)
			{
				for (int x = 1; x < dimension.getX() - 1; x++)
				{
					for (int z = 1; z < dimension.getZ() - 1; z++)
					{
						positions.add(new BlockPos(x, y, z));
					}

				}

			}

			return positions;
		};
	}

	public static PreviewSelector planeSpiralCCW(Function<IPreviewBuilder, BlockPos> pivotFunction, ToIntFunction<IPreviewBuilder> limitFunction)
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int limit = Math.min(Math.max(limitFunction.applyAsInt(builder), 0), dimension.getX() * dimension.getZ());
			List<BlockPos> positions = new ArrayList<>();

			if (limit == 0)
			{
				return positions;
			}

			BlockPos.Mutable cursor = pivotFunction.apply(builder).mutable();

			Direction direction = Direction.EAST;
			int distance = 1;

			while (positions.size() < limit)
			{
				for (int side = 0; side < 2 && positions.size() < limit; side++)
				{
					for (int step = 0; step < distance && positions.size() < limit; step++)
					{
						positions.add(cursor.immutable());
						cursor.move(direction);
					}

					direction = direction.getCounterClockWise();
				}

				distance++;
			}

			return positions;
		};
	}

	public static PreviewSelector column(Function<IPreviewBuilder, BlockPos> pivotFunciton, ToIntFunction<IPreviewBuilder> heightFunction)
	{
		return builder ->
		{
			BlockPos bottom = pivotFunciton.apply(builder);
			int height = heightFunction.applyAsInt(builder);
			List<BlockPos> positions = new ArrayList<>();

			for (int y = 0; y < height; y++)
			{
				positions.add(bottom.above(y));
			}

			return positions;
		};
	}

	public static PreviewSelector topCorners()
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int maxX = dimension.getX() - 1;
			int maxY = dimension.getY() - 1;
			int maxZ = dimension.getZ() - 1;
			return Arrays.asList(new BlockPos(0, maxY, 0), new BlockPos(maxX, maxY, 0), new BlockPos(0, maxY, maxZ), new BlockPos(maxX, maxY, maxZ));
		};
	}

	public static PreviewSelector topEdges()
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int maxX = dimension.getX() - 1;
			int maxY = dimension.getY() - 1;
			int maxZ = dimension.getZ() - 1;
			List<BlockPos> positions = new ArrayList<>();

			for (int x = 1; x < maxX; x++)
			{
				positions.add(new BlockPos(x, maxY, 0));
				positions.add(new BlockPos(x, maxY, maxZ));
			}

			for (int z = 1; z < maxZ; z++)
			{
				positions.add(new BlockPos(0, maxY, z));
				positions.add(new BlockPos(maxX, maxY, z));
			}

			return positions;
		};
	}

	public static PreviewSelector shellSidesCCW()
	{
		return builder ->
		{
			Vector3i dimension = builder.getDimension();
			int maxX = dimension.getX() - 1;
			int maxY = dimension.getY() - 1;
			int maxZ = dimension.getZ() - 1;
			List<BlockPos> positions = new ArrayList<>();

			for (int y = 1; y < maxY; y++)
			{
				for (int x = 1; x < maxX; x++)
				{
					positions.add(new BlockPos(x, y, maxZ));
				}

				for (int z = maxZ - 1; z >= 1; z--)
				{
					positions.add(new BlockPos(maxX, y, z));
				}

				for (int x = maxX - 1; x >= 1; x--)
				{
					positions.add(new BlockPos(x, y, 0));
				}

				for (int z = 1; z < maxZ; z++)
				{
					positions.add(new BlockPos(0, y, z));
				}

			}

			for (int y : new int[]{0, maxY})
			{
				for (int z = maxZ - 1; z >= 1; z--)
				{
					for (int x = maxX - 1; x >= 1; x--)
					{
						positions.add(new BlockPos(x, y, z));
					}

				}

			}

			return positions;
		};
	}

}
