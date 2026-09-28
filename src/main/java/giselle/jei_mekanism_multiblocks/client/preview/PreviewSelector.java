package giselle.jei_mekanism_multiblocks.client.preview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface PreviewSelector
{
	List<BlockPos> select(IPreviewBuilder builder);

	default PreviewSelector and(PreviewSelector other)
	{
		return context ->
		{
			Set<BlockPos> accepted = new HashSet<>(other.select(context));
			return this.select(context).stream().filter(accepted::contains).collect(java.util.stream.Collectors.toList());
		};
	}

}
