package giselle.jei_mekanism_multiblocks.common.util;

public enum VolumeUnit
{
	MILLI("Milli", "m", -1),
	ONE("", "", 0),
	KILO("Kilo", "K", 1),
	MEGA("Mega", "M", 2),
	GIGA("Giga", "G", 3),
	TERA("Tera", "T", 4),
	PETA("Peta", "P", 5),
	// EOL
	;

	private String fullName;
	private String shortName;
	private int multiplier;

	private VolumeUnit(String fullName, String shortName, int multiplier)
	{
		this.fullName = fullName;
		this.shortName = shortName;
		this.multiplier = multiplier;
	}

	public String getShortName()
	{
		return this.shortName;
	}

	public String getFullName()
	{
		return fullName;
	}

	public int getMultiplier()
	{
		return this.multiplier;
	}

}
