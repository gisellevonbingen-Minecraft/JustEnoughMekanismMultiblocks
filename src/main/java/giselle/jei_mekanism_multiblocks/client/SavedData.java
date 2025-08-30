package giselle.jei_mekanism_multiblocks.client;

import java.io.File;
import java.io.IOException;

import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public class SavedData
{
	private static final File FILE = FMLPaths.CONFIGDIR.get().resolve(JEI_MekanismMultiblocks.MODID + "-saved_data.nbt").toFile();
	private static final CompoundTag MULTIBLOCKS = new CompoundTag();

	private static final String TAG_MULTIBLOCKS = "multiblocks";

	public static boolean hasMultiblock(ResourceLocation name)
	{
		return MULTIBLOCKS.contains(name.toString(), Tag.TAG_COMPOUND);
	}

	public static CompoundTag getMultiblock(ResourceLocation name)
	{
		return MULTIBLOCKS.getCompound(name.toString());
	}

	public static void setMultiblockData(ResourceLocation name, CompoundTag tag)
	{
		MULTIBLOCKS.put(name.toString(), tag);
	}

	public static void load()
	{
		try
		{
			if (FILE.exists())
			{
				CompoundTag tag = NbtIo.readCompressed(FILE.toPath(), NbtAccounter.unlimitedHeap());
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
			CompoundTag tag = new CompoundTag();
			tag.put(TAG_MULTIBLOCKS, MULTIBLOCKS);
			NbtIo.writeCompressed(tag, FILE.toPath());
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
