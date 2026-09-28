package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

public class PreviewLevel implements IBlockDisplayReader
{
	private static boolean CONSTANT_AMBIENT_LIGHT = true;

	private final Vector3i dimension;
	private final Map<BlockPos, BlockState> blocks;

	private int renderHeight;

	public PreviewLevel(IPreviewBuilder builder)
	{
		this.dimension = builder.getDimension();
		this.blocks = builder.getBlocks();
		this.renderHeight = this.dimension.getY();
	}

	public Vector3i getDimension()
	{
		return this.dimension;
	}

	public int getRenderHeight()
	{
		return this.renderHeight;
	}

	public void setRenderHeight(int renderHeight)
	{
		this.renderHeight = MathHelper.clamp(renderHeight, 0, this.dimension.getY());
	}

	@Override
	public BlockState getBlockState(BlockPos pos)
	{
		if (pos.getX() < 0 || pos.getX() >= this.dimension.getX() || pos.getY() < 0 || pos.getY() >= this.renderHeight || pos.getZ() < 0 || pos.getZ() >= this.dimension.getZ())
		{
			return Blocks.AIR.defaultBlockState();
		}

		return this.blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
	}

	public Map<BlockPos, BlockState> getBlockStates()
	{
		return new HashMap<>(this.blocks);
	}

	@Override
	public FluidState getFluidState(BlockPos pos)
	{
		return this.getBlockState(pos).getFluidState();
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos pos)
	{
		return null;
	}

	public int getHeight()
	{
		return this.dimension.getY();
	}

	public int getMinBuildHeight()
	{
		return 0;
	}

	@Override
	public float getShade(Direction direction, boolean shade)
	{
		if (shade)
		{
			switch (direction)
			{
				case DOWN:
					return CONSTANT_AMBIENT_LIGHT ? 0.9F : 0.5F;
				case UP:
					return CONSTANT_AMBIENT_LIGHT ? 0.9F : 1.0F;
				case NORTH:
				case SOUTH:
					return 0.8F;
				case WEST:
				case EAST:
					return 0.6F;
				default:
					return 1.0F;
			}
		}

		return CONSTANT_AMBIENT_LIGHT ? 0.9F : 1.0F;
	}

	@Override
	public WorldLightManager getLightEngine()
	{
		return null;
	}

	@Override
	public int getBrightness(LightType layer, BlockPos pos)
	{
		return 15;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int amount)
	{
		return 15;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver resolver)
	{
		return 0xFFFFFFFF;
	}

}
