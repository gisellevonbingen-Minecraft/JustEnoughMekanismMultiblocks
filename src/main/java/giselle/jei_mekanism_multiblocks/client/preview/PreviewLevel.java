package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class PreviewLevel implements BlockAndTintGetter
{
	private static boolean CONSTANT_AMBIENT_LIGHT = true;

	private final Vec3i dimension;
	private final Map<BlockPos, BlockState> blocks;

	private int renderHeight;

	public PreviewLevel(IPreviewBuilder builder)
	{
		this.dimension = builder.getDimension();
		this.blocks = builder.getBlocks();
		this.renderHeight = this.dimension.getY();
	}

	public Vec3i getDimension()
	{
		return this.dimension;
	}

	public int getRenderHeight()
	{
		return this.renderHeight;
	}

	public void setRenderHeight(int renderHeight)
	{
		this.renderHeight = Mth.clamp(renderHeight, 0, this.dimension.getY());
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
	public BlockEntity getBlockEntity(BlockPos pos)
	{
		return null;
	}

	@Override
	public int getHeight()
	{
		return this.dimension.getY();
	}

	@Override
	public int getMinBuildHeight()
	{
		return 0;
	}

	@Override
	public float getShade(Direction direction, boolean shade)
	{
		if (shade)
		{
			return switch (direction)
			{
				case DOWN -> CONSTANT_AMBIENT_LIGHT ? 0.9F : 0.5F;
				case UP -> CONSTANT_AMBIENT_LIGHT ? 0.9F : 1.0F;
				case NORTH, SOUTH -> 0.8F;
				case WEST, EAST -> 0.6F;
				default -> 1.0F;
			};
		}
		else
		{
			return CONSTANT_AMBIENT_LIGHT ? 0.9F : 1.0F;
		}

	}

	@Override
	public LevelLightEngine getLightEngine()
	{
		return null;
	}

	@Override
	public int getBrightness(LightLayer layer, BlockPos pos)
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
