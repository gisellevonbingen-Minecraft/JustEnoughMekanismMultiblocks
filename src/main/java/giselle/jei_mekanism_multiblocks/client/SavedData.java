package giselle.jei_mekanism_multiblocks.client;

import java.io.File;
import java.io.IOException;

import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.loading.FMLPaths;

public class SavedData
{
	private static final File FILE = FMLPaths.CONFIGDIR.get().resolve(JEI_MekanismMultiblocks.MODID + "-saved_data.nbt").toFile();
	private static final CompoundNBT MULTIBLOCKS = new CompoundNBT();

	private static final String TAG_MULTIBLOCKS = "multiblocks";

	public static boolean hasMultiblock(ResourceLocation name)
	{
		return MULTIBLOCKS.contains(name.toString(), NBT.TAG_COMPOUND);
	}

	public static CompoundNBT getMultiblock(ResourceLocation name)
	{
		return MULTIBLOCKS.getCompound(name.toString());
	}

	public static void setMultiblockData(ResourceLocation name, CompoundNBT tag)
	{
		MULTIBLOCKS.put(name.toString(), tag);
	}

	public static void load()
	{
		try
		{
			if (FILE.exists())
			{
				CompoundNBT tag = CompressedStreamTools.read(FILE);
				MULTIBLOCKS.merge(tag.getCompound(TAG_MULTIBLOCKS));
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void save()
	{
		try
		{
			CompoundNBT tag = new CompoundNBT();
			tag.put(TAG_MULTIBLOCKS, MULTIBLOCKS);
			CompressedStreamTools.write(tag, FILE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private SavedData()
	{

	}

}
